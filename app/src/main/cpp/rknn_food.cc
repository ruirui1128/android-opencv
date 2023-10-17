


#include <cstdio>
#include <cstdlib>
#include <jni.h>
#include "opencv2/core/mat.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/imgproc.hpp"
#include "rknn_food.h"
#include "object_tracker/track_link.h"
#include <android/log.h>

rknn_context ctx_food = 0;
unsigned char *model;
int model_len = 0;
int ret;
rknn_input_output_num io_num;

std::vector<std::string> cp_names; //菜品名称 vector
std::vector<cv::Mat> mat_vectors;  //菜品特征 vector

static void dump_tensor_attr(rknn_tensor_attr *attr) {
    printf("  index=%d, name=%s, n_dims=%d, dims=[%d, %d, %d, %d], n_elems=%d, size=%d, fmt=%s, type=%s, qnt_type=%s, "
           "zp=%d, scale=%f\n",
           attr->index, attr->name, attr->n_dims, attr->dims[0], attr->dims[1], attr->dims[2],
           attr->dims[3],
           attr->n_elems, attr->size, get_format_string(attr->fmt), get_type_string(attr->type),
           get_qnt_type_string(attr->qnt_type), attr->zp, attr->scale);
}

// 特征值比对 计算余弦相似度
static double calculateCosineSimilarity(const cv::Mat &vector1, const cv::Mat &vector2) {
    if (vector1.rows != vector2.rows || vector1.cols != vector2.cols) {
        std::cerr << "Input vectors must have the same dimensions." << std::endl;
        return 0.0;
    }
    double dotProduct = vector1.dot(vector2);
    double normVector1 = cv::norm(vector1);
    double normVector2 = cv::norm(vector2);
    double similarity = dotProduct / (normVector1 * normVector2);
    return similarity;
}


int initFoodModel(const char *model_path) {
    model = load_model(model_path, &model_len);
    ret = rknn_init(&ctx_food, model, model_len, 0, NULL);
    if (ret < 0) {
        printf("rknn_init fail! ret=%d\n", ret);
        LOGI("RKNN rknn_init fail! ret=%d\n", ret);
        return -1;
    }

    // rknn_input_output_num io_num;
    ret = rknn_query(ctx_food, RKNN_QUERY_IN_OUT_NUM, &io_num, sizeof(io_num));
    if (ret != RKNN_SUCC) {
        printf("rknn_query fail! ret=%d\n", ret);
        LOGI("rknn_query fail! ret=%d\n", ret);
        return -1;
    }
    printf("model input num: %d, output num: %d\n", io_num.n_input, io_num.n_output);

    printf("input tensors:\n");
    rknn_tensor_attr input_attrs[io_num.n_input];
    memset(input_attrs, 0, sizeof(input_attrs));
    for (int i = 0; i < io_num.n_input; i++) {
        input_attrs[i].index = i;
        ret = rknn_query(ctx_food, RKNN_QUERY_INPUT_ATTR, &(input_attrs[i]),
                         sizeof(rknn_tensor_attr));
        if (ret != RKNN_SUCC) {
            printf("rknn_query fail! ret=%d\n", ret);
            return -1;
        }
        dump_tensor_attr(&(input_attrs[i]));
    }

    printf("output tensors:\n");
    rknn_tensor_attr output_attrs[io_num.n_output];
    memset(output_attrs, 0, sizeof(output_attrs));
    for (int i = 0; i < io_num.n_output; i++) {
        output_attrs[i].index = i;
        ret = rknn_query(ctx_food, RKNN_QUERY_OUTPUT_ATTR, &(output_attrs[i]),
                         sizeof(rknn_tensor_attr));
        if (ret != RKNN_SUCC) {
            printf("rknn_query fail! ret=%d\n", ret);
            return -1;
        }
        dump_tensor_attr(&(output_attrs[i]));
    }

    return 0;

}


unsigned char *load_model(const char *filename, int *model_size) {
    FILE *fp = fopen(filename, "rb");
    if (fp == nullptr) {
        LOGI("RKNN fopen %s fail!\n", filename);
        return NULL;
    }
    fseek(fp, 0, SEEK_END);
    int model_len = ftell(fp);
    unsigned char *model = (unsigned char *) malloc(model_len);
    fseek(fp, 0, SEEK_SET);
    if (model_len != fread(model, 1, model_len, fp)) {
        printf("fread %s fail!\n", filename);
        free(model);
        return NULL;
    }
    *model_size = model_len;
    if (fp) {
        fclose(fp);
    }
    return model;
}


int foodStorage(jlong img_obj_addr, const char *img_name, const std::string &file_path) {
    cv::Mat outputMat = processImage(img_obj_addr);
    if (outputMat.empty()) {
        return -1;
    }
    return appendMatToXML(outputMat, img_name, file_path);
}


//对检测到的目标 提取 128维 向量
cv::Mat processImage(jlong matAddr) {
//    cv::Mat orig_img = imread(img_path, cv::IMREAD_COLOR);
//    if (!orig_img.data) {
//        printf("cv::imread %s fail!\n", img_path);
//    }
    cv::Mat &orig_img = *(cv::Mat *) matAddr;
    cv::cvtColor(orig_img, orig_img, cv::IMREAD_COLOR);
    cv::Mat orig_img_rgb;
    cv::cvtColor(orig_img, orig_img_rgb, cv::COLOR_BGR2RGB);

    cv::Mat img = orig_img_rgb.clone();
    if (orig_img.cols != MODEL_IN_WIDTH || orig_img.rows != MODEL_IN_HEIGHT) {
        printf("resize %d %d to %d %d\n", orig_img.cols, orig_img.rows, MODEL_IN_WIDTH,
               MODEL_IN_HEIGHT);
        cv::resize(orig_img, img, cv::Size(MODEL_IN_WIDTH, MODEL_IN_HEIGHT), 0, 0,
                   cv::INTER_LINEAR);
    }


    rknn_input inputs[1];
    memset(inputs, 0, sizeof(inputs));
    inputs[0].index = 0;
    inputs[0].type = RKNN_TENSOR_UINT8;
    inputs[0].size = img.cols * img.rows * img.channels() * sizeof(uint8_t);
    inputs[0].fmt = RKNN_TENSOR_NHWC;
    inputs[0].buf = img.data;

    ret = rknn_inputs_set(ctx_food, io_num.n_input, inputs);
    if (ret < 0) {
        printf("rknn_input_set fail! ret=%d\n", ret);

    }


    ret = rknn_run(ctx_food, nullptr);
    if (ret < 0) {
        printf("rknn_run fail! ret=%d\n", ret);

    }

    // Get Output
    rknn_output outputs[1];

    memset(outputs, 0, sizeof(outputs));
    outputs[0].want_float = 1;
    ret = rknn_outputs_get(ctx_food, 1, outputs, NULL);
    if (ret < 0) {
        printf("rknn_outputs_get fail! ret=%d\n", ret);

    }

    float *buffer = (float *) outputs[0].buf;
    uint32_t bufferSize = outputs[0].size / 4;

    std::cout << "bufferSize:\n" << bufferSize << std::endl;

    cv::Mat outputMat(1, bufferSize, CV_32FC1);

    //output 向量L2归一化
    for (uint32_t i = 0; i < bufferSize; i++) {
        outputMat.at<float>(0, i) = buffer[i];
    }
    cv::normalize(outputMat, outputMat, cv::NORM_L2);
    // printf("Mat2::");
    // std::cout << "Output Mat:\n" << outputMat << std::endl;

    rknn_outputs_release(ctx_food, 1, outputs);
    return outputMat;
}


// 提取的Mat 文件 保存到 XML 文件中
int
appendMatToXML(const cv::Mat &outputMat, const std::string &cp_name, const std::string &file_path) {
    LOGI("==========appendMatToXML==============");

    cv::FileStorage fs(file_path, cv::FileStorage::APPEND);

    if (!fs.isOpened()) {
        std::cerr << "Failed to open cp_xml for appending!" << std::endl;

        LOGI("RKNN  Failed to open cp_xml for appending.");
        return -1;
    } else {
        LOGI("RKNN  ok to open cp_xml for appending:%s", file_path.c_str());
    }
    auto now = std::chrono::system_clock::now();
    auto timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
            now.time_since_epoch()).count();
    std::stringstream cp_name_with_timestamp;
    cp_name_with_timestamp << cp_name << "_" << timestamp;

    fs << cp_name_with_timestamp.str() << outputMat;

    fs.release();
    LOGI("RKNN  =========================菜品入库%s=", cp_name.c_str());
    return 0;
}


// 比对 Mat  得到 cp_names 和 outputMat 的vectors
void readMatVectorsFromXML(const std::string &file_path) {
    cv::FileStorage fs(file_path, cv::FileStorage::READ);
    if (!fs.isOpened()) {
        std::cerr << "Failed to open xml for reading!" << std::endl;
        LOGI("Failed to open xml for reading!");
        return;
    }
    cv::FileNode rootNode = fs.root();
    std::cout << rootNode.empty() << std::endl;
    if (rootNode.empty() || !rootNode.isMap()) {
        std::cerr << "Invalid XML file format." << std::endl;
        LOGI("Invalid XML file format.");
        return;
    }
    for (cv::FileNodeIterator it = rootNode.begin(); it != rootNode.end(); ++it) {
        cv::FileNode node = *it;
        std::string cp_name = node.name();
        size_t underscorePos = cp_name.find('_');

        cp_names.push_back(cp_name.substr(0, underscorePos));

        cv::Mat mat_vector;
        node >> mat_vector;

        mat_vectors.push_back(mat_vector);

    }
    // size_t nvectorLength = cp_names.size();
    // std::cout << "cp_names length: " << nvectorLength << std::endl;

    // size_t vectorLength = mat_vectors.size();
    // std::cout << "Vector length: " << vectorLength << std::endl;
    LOGI("======readMatVectorsFromXML=========");
    fs.release();
}

std::pair<double, std::string> recognize(jlong matAddr) {
    cv::Mat outputMat = processImage(matAddr);
    return matScores(outputMat, 0.72);
//    LOGI("=================recognize============Pair: %f, %s", pair.first, pair.second.c_str());
}


std::pair<double, std::string> matScores(
        cv::Mat &outputMat,
        double thred) {

    std::cout << "ok" << std::endl;
    std::vector<double> scores;
    std::pair<double, std::string> result;
    for (const cv::Mat &mat: mat_vectors) {

        double s = calculateCosineSimilarity(mat, outputMat);
        scores.push_back(s);
    }
    // std::cout<<scores<<std::endl;

    auto max_element = std::max_element(scores.begin(), scores.end());

    if (max_element != scores.end()) {
        // 计算最大值的索引
        int max_index = std::distance(scores.begin(), max_element);
        double max_score = *max_element; // 解引用迭代器获取最大值
        if (max_score >= thred) {
            result = std::make_pair(max_score, cp_names[max_index]);
        } else {
            result = std::make_pair(0.0, "Not Recognize");
        }
        // std::cout << "最大值是: " << max_score << "，名称是: " << cp_names[max_index] << std::endl;
    } else {
        result = std::make_pair(0.0, "Not Recognize");
    }
    std::cout << "Score: " << result.first << ", Name: " << result.second << std::endl;
    return result;
}



