package com.ahut;

import com.ahut.spi.impl.LoggerService;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        testSpi();
    }

    public static void testSpi(){
        LoggerService service = LoggerService.getService();
        service.info("test info msg");
        service.debug("test debug msg");
    }
}
