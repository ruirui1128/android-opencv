//
// Created by rui on 2023-10-09.
//



#ifndef OPENCVTEST_RKNN_FOOD_H
#define OPENCVTEST_RKNN_FOOD_H

#include "opencv2/core/core.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/imgproc.hpp"
#include "rknn_api.h"
#include "opencv2/opencv.hpp"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <chrono>
#include <sstream>
#include <fstream>
#include <iostream>
#include <string>
#include <algorithm>


const int MODEL_IN_WIDTH = 224;
const int MODEL_IN_HEIGHT = 224;
const int MODEL_IN_CHANNELS = 3;



int test();

int initFoodModel(const char* model_path);

unsigned char* load_model(const char* filename, int* model_size);

int foodStorage(jlong img_obj_addr,const char *img_name,const std::string &file_path);  //菜品入库

cv::Mat processImage(jlong matAddr);

int appendMatToXML(const cv::Mat &outputMat, const std::string &cp_name,const std::string &file_path);//Mat向量,菜品名称

void readMatVectorsFromXML(const std::string &file_path);
//void readMatVectorsFromXML(std::vector<std::string> &cp_names, std::vector<cv::Mat> &mat_vectors,const std::string &file_path);

//void readMatVectors(const std::string &file_path);

std::pair<double, std::string> recognize(jlong matAddr);

std::pair<double, std::string> matScores(
                                         cv::Mat &outputMat,
                                         double thred);


#endif //OPENCVTEST_RKNN_FOOD_H
