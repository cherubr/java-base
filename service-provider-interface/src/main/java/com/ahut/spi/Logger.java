package com.ahut.spi;

/**
 * @author Sumin.G
 * @title: Logger
 * @projectName java-base
 * @description: TODO
 * @date 2022/9/69:40
 */
public interface Logger {
    /**
     * 打印info级别信息
     * @param msg 打印信息
     */
    void info(String msg);


    /**
     * 打印debug级别信息
     * @param msg 打印信息
     */
    void debug(String msg);
}
