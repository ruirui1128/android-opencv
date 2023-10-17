package com.mind.open.yolo;


import static com.mind.open.rga.HALDefine.CAMERA_PREVIEW_HEIGHT;
import static com.mind.open.rga.HALDefine.CAMERA_PREVIEW_WIDTH;
import static java.lang.System.arraycopy;

import android.content.res.AssetManager;
import android.graphics.RectF;


import com.mind.open.tracker.ObjectTracker;

import java.io.IOException;
import java.util.ArrayList;

public class InferenceResult {

    OutputBuffer mOutputBuffer;
    ArrayList<Recognition> recognitions = null;
    private boolean mIsVaild = false;   //是否需要重新计算
    public PostProcess mPostProcess = new PostProcess();
    private ObjectTracker mSSDObjectTracker;

    public void init(AssetManager assetManager) throws IOException {
        mOutputBuffer = new OutputBuffer();

        mPostProcess.init(assetManager);

//        mSSDObjectTracker = new ObjectTracker(CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT, 3);
    }

    public void reset() {
        if (recognitions != null) {
            recognitions.clear();
            mIsVaild = true;
        }
        mSSDObjectTracker = new ObjectTracker(CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT, 3);
    }

    public synchronized void setResult(OutputBuffer outputs) {

        if (mOutputBuffer.mGrid0Out == null) {
            mOutputBuffer.mGrid0Out = outputs.mGrid0Out.clone();
            mOutputBuffer.mGrid1Out = outputs.mGrid1Out.clone();
            mOutputBuffer.mGrid2Out = outputs.mGrid2Out.clone();
        } else {
            arraycopy(outputs.mGrid0Out, 0, mOutputBuffer.mGrid0Out, 0,
                    outputs.mGrid0Out.length);
            arraycopy(outputs.mGrid1Out, 0, mOutputBuffer.mGrid1Out, 0,
                    outputs.mGrid1Out.length);
            arraycopy(outputs.mGrid2Out, 0, mOutputBuffer.mGrid2Out, 0,
                    outputs.mGrid2Out.length);
        }
        mIsVaild = false;
    }

    public synchronized ArrayList<Recognition> getResult(InferenceWrapper mInferenceWrapper) {
        if (!mIsVaild) {
            mIsVaild = true;

            recognitions = mInferenceWrapper.postProcess(mOutputBuffer);

            recognitions = mSSDObjectTracker.tracker(recognitions);
        }

        return recognitions;
    }


    public synchronized ArrayList<Recognition> getResult2(InferenceWrapper mInferenceWrapper) {
        if (!mIsVaild) {
            mIsVaild = true;

            recognitions = mInferenceWrapper.postProcess(mOutputBuffer);

            recognitions = mSSDObjectTracker.tracker(recognitions);

            recognitions.forEach(f -> {
                f.title = mPostProcess.getLabelTitle(f.id);
            });
        }

        return recognitions;
    }


    public static class OutputBuffer {
        public byte[] mGrid0Out;
        public byte[] mGrid1Out;
        public byte[] mGrid2Out;
    }

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    public static class Recognition {

        private String title = "";

        public String getTitle() {
            return title;
        }


        private double score = 0;

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        private int trackId = 0;

        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final int id;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;

        public Recognition(
                final int id, final Float confidence, final RectF location) {
            this.id = id;
            this.confidence = confidence;
            this.location = location;
            // TODO -- add name field, and show it.
        }


        public int getId() {
            return id;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        public void setTrackId(int trackId) {
            this.trackId = trackId;
        }

        public int getTrackId() {
            return this.trackId;
        }

        @Override
        public String toString() {
            String resultString = "";

            resultString += "[" + id + "] ";

            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }
    }

    /**
     * Detected objects, returned from native yolo_post_process
     */
    public static class DetectResultGroup {
        /**
         * detected objects count.
         */
        public int count = 0;

        /**
         * id for each detected object.
         */
        public int[] ids;

        /**
         * score for each detected object.
         */
        public float[] scores;

        /**
         * box for each detected object.
         */
        public float[] boxes;


    }
}
