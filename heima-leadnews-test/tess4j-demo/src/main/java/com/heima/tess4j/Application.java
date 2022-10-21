package com.heima.tess4j;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class Application {

    public static void main(String[] args) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("D:\\workspace\\tessdata");
        tesseract.setLanguage("chi_sim");
        System.out.println(tesseract);

        String result = tesseract.doOCR(new File("D:\\黑马头条\\day03-自媒体文章发布\\讲义\\自媒体文章发布.assets\\image-20210426110728659.png"));

        System.out.println(result.replaceAll("\\r|\\n","-"));
    }
}
