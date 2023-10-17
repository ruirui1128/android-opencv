package com.mind.open.yolo;

import android.graphics.RectF;
import android.util.Log;


import com.mind.open.RKnnJni;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by randall on 18-4-18.
 */

public class InferenceWrapper {
    private final String TAG = "rkyolo.InferenceWrapper";

//    static {
//        System.loadLibrary("open");
//    }

    InferenceResult.OutputBuffer mOutputs;
    ArrayList<InferenceResult.Recognition> mRecognitions = new ArrayList<InferenceResult.Recognition>();
    InferenceResult.DetectResultGroup mDetectResults;

    public int OBJ_NUMB_MAX_SIZE = 64;
//    public int inf_count = 0;
//    public int post_count = 0;
//    public long inf_time = 0;
//    public long post_time = 0;


    public InferenceWrapper() {
        initModel();
    }

    private int initModel() {
        mOutputs = new InferenceResult.OutputBuffer();
        mOutputs.mGrid0Out = new byte[255 * 80 * 80];
        mOutputs.mGrid1Out = new byte[255 * 40 * 40];
        mOutputs.mGrid2Out = new byte[255 * 20 * 20];

        return 0;


    }


    public void deinit() {
        //    native_deinit();
        RKnnJni.Companion.getJni().deInitDetectModel();
        mOutputs.mGrid0Out = null;
        mOutputs.mGrid1Out = null;
        mOutputs.mGrid2Out = null;
        mOutputs = null;

    }

    public InferenceResult.OutputBuffer run(byte[] inData) {

        // native_run(inData, mOutputs.mGrid0Out, mOutputs.mGrid1Out, mOutputs.mGrid2Out);

        RKnnJni.Companion.getJni().detectRun(inData, mOutputs.mGrid0Out, mOutputs.mGrid1Out, mOutputs.mGrid2Out);

        return mOutputs;
    }

    public ArrayList<InferenceResult.Recognition> postProcess(InferenceResult.OutputBuffer outputs) {
        ArrayList<InferenceResult.Recognition> recognitions = new ArrayList<InferenceResult.Recognition>();

        mDetectResults = new InferenceResult.DetectResultGroup();
        mDetectResults.count = 0;
        mDetectResults.ids = new int[OBJ_NUMB_MAX_SIZE];
        mDetectResults.scores = new float[OBJ_NUMB_MAX_SIZE];
        mDetectResults.boxes = new float[4 * OBJ_NUMB_MAX_SIZE];

        if (null == outputs || null == outputs.mGrid0Out || null == outputs.mGrid1Out
                || null == outputs.mGrid2Out) {
            return recognitions;
        }
//
//        int count = native_post_process(outputs.mGrid0Out, outputs.mGrid1Out, outputs.mGrid2Out,
//                mDetectResults.ids, mDetectResults.scores, mDetectResults.boxes);

        int count = RKnnJni.Companion.getJni().detectPostProcess(outputs.mGrid0Out, outputs.mGrid1Out, outputs.mGrid2Out,
                mDetectResults.ids, mDetectResults.scores, mDetectResults.boxes
        );

        if (count < 0) {
            Log.w(TAG, "post_process may fail.");
            mDetectResults.count = 0;
        } else {
            mDetectResults.count = count;
        }


        for (int i = 0; i < count; ++i) {
            RectF rect = new RectF();
            rect.left = mDetectResults.boxes[i * 4 + 0];
            rect.top = mDetectResults.boxes[i * 4 + 1];
            rect.right = mDetectResults.boxes[i * 4 + 2];
            rect.bottom = mDetectResults.boxes[i * 4 + 3];

            InferenceResult.Recognition recog = new InferenceResult.Recognition(mDetectResults.ids[i],
                    mDetectResults.scores[i], rect);
            recognitions.add(recog);
        }


        return recognitions;
    }

//    private native int navite_init(int im_height, int im_width, int im_channel, String modelPath);
//
//    private native void native_deinit();
//
//    private native int native_run(byte[] inData, byte[] grid0Out, byte[] grid1Out, byte[] grid2Out);
//
//    private native int native_post_process(byte[] grid0Out, byte[] grid1Out, byte[] grid2Out,
//                                           int[] ids, float[] scores, float[] boxes);

}