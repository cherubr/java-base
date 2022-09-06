package com.ahut.spi.server;

import com.ahut.spi.Logger;

/**
 * @author Sumin.G
 * @title: Logback
 * @projectName java-base
 * @description: TODO
 * @date 2022/9/610:16
 */
public class Logback implements Logger {
    @Override
    public void info(String s) {
        System.out.println("Logback info 打印日志：" + s);
    }

    @Override
    public void debug(String s) {
        System.out.println("Logback debug 打印日志：" + s);
    }
}
