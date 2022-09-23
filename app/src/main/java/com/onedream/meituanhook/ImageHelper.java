package com.onedream.meituanhook;

import android.graphics.Rect;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageHelper {

    public static Rect singleMatching(Mat source, Mat target, float matching, String fileName) {
        //模板图片
        Mat clone = target.clone();
        if (source.empty() || target.empty()) {
            Log.e(">>>>", "资源为null");
            return null;
        }
        int templatW, templatH, resultH, resultW;
        templatW = source.width();
        templatH = source.height();
        resultH = target.rows() - source.rows() + 1;
        resultW = target.cols() - source.cols() + 1;
        //匹配结果的大小
        Mat result = new Mat(new Size(resultH, resultW), CvType.CV_32FC1);
        //是标准相关性系数匹配  值越大越匹配
        Imgproc.matchTemplate(clone, source, result, Imgproc.TM_CCOEFF_NORMED);
        //匹配结果，最小到最大
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        if (mmr.maxVal > matching) {
            //在原图上的对应模板可能位置画一个绿色矩形
            Imgproc.rectangle(target, mmr.maxLoc, new Point(mmr.maxLoc.x + templatW, mmr.maxLoc.y + templatH), new Scalar(0, 255, 0), 2);
            Log.e(">>>>", "匹配的值：" + mmr.maxVal + "   ------坐标：" + mmr.maxLoc.x + "," + mmr.maxLoc.y);
            //
            return new Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, (int) mmr.maxLoc.x + templatW, (int) mmr.maxLoc.y + templatH);
        }
        return null;
    }


    /**
     * 匹配模板图片， 图片须时jpg格式的，否则会出异常
     *
     * @param source   源图片
     * @param target   模板图片
     * @param matching 匹配度 0-1 之间
     * @return
     */
    public static Mat matching(Mat source, Mat target, float matching, String fileName) {
        //模板图片
        Mat clone = target.clone();
        if (source.empty() || target.empty()) {
            Log.e(">>>>", "资源为null");
            return target;
        }
        int templatW, templatH, resultH, resultW;
        templatW = source.width();
        templatH = source.height();
        resultH = target.rows() - source.rows() + 1;
        resultW = target.cols() - source.cols() + 1;
        //匹配结果的大小
        Mat result = new Mat(new Size(resultH, resultW), CvType.CV_32FC1);
        //是标准相关性系数匹配  值越大越匹配
        Imgproc.matchTemplate(clone, source, result, Imgproc.TM_CCOEFF_NORMED);
        //匹配结果，最小到最大
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        if (mmr.maxVal > matching) {
            //在原图上的对应模板可能位置画一个绿色矩形
            Imgproc.rectangle(target, mmr.maxLoc, new Point(mmr.maxLoc.x + templatW, mmr.maxLoc.y + templatH), new Scalar(0, 255, 0), 2);
            Log.e(">>>>", "匹配的值：" + mmr.maxVal + "   ------坐标：" + mmr.maxLoc.x + "," + mmr.maxLoc.y);
            //
            ClickPointHelper.INSTANCE.setTestClickRect(new Rect((int)mmr.maxLoc.x,(int) mmr.maxLoc.y, (int)mmr.maxLoc.x + templatW, (int)mmr.maxLoc.y + templatH));
        }
        //第几个目标图片
        int count = 0;
        //找出全部相似度照片
        while (mmr.maxVal > matching) {
            mmr = getMaxLoc(clone, source, templatW, templatH, mmr.maxLoc);
            if (mmr.maxVal > matching) {
                count += 1;
                //画一个绿色的矩形
                Imgproc.rectangle(target, mmr.maxLoc, new Point(mmr.maxLoc.x + templatW, mmr.maxLoc.y + templatH), new Scalar(0, 255, 0), 2);
                Imgproc.putText(target, "" + count, new Point(mmr.maxLoc.x, mmr.maxLoc.y), 1, 3, new Scalar(0, 255, 0), 2);
                Log.e(">>>>", "匹配的值：" + mmr.maxVal + "   ------坐标：" + mmr.maxLoc.x + "," + mmr.maxLoc.y);
            } else {
                break;
            }
        }
        //将结果输出到对应位置
        Imgcodecs.imwrite(fileName, target);
        return target;
    }

    /**
     * 获取坐标
     * @param clone
     * @param result
     * @param templatW
     * @param templatH
     * @param maxLoc
     * @return
     */
    private static Core.MinMaxLocResult getMaxLoc(Mat clone, Mat result, int templatW, int templatH, Point maxLoc) {
        int startY, startX, endY, endX;

        //计算大矩形的坐标
        startY = (int) maxLoc.y;
        startX = (int) maxLoc.x;

        //计算大矩形的的坐标
        endY = (int) maxLoc.y + templatH;
        endX = (int) maxLoc.x + templatW;

        //将大矩形内部 赋值为最大值 使得 以后找的最小值 不会位于该区域  避免找到重叠的目标
        int ch = clone.channels();
        //通道数 (灰度: 1, RGB: 3, etc.)
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                double[] data = clone.get(j, i);    //读取像素值，并存储在double数组中
                for (int k = 0; k < ch; k++) {        //RGB值或灰度值
                    data[k] = 255;      //对每个像素值（灰度值或RGB通道值，取值0~255）进行处理
                }
                clone.put(j, i, data);         //把处理后的像素值写回到Mat
            }
        }

        int resultH = clone.rows() - result.rows() + 1;
        int resultW = clone.cols() - result.cols() + 1;
        Mat result2 = new Mat(new Size(resultH, resultW), CvType.CV_32FC1);
        Imgproc.matchTemplate(clone, result, result2, Imgproc.TM_CCOEFF_NORMED);   //是标准相关性系数匹配  值越大越匹配
        //查找result中的最大值 及其所在坐标
        return Core.minMaxLoc(result2);
    }
}
