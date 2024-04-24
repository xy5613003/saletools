package com.fight;

import lombok.extern.log4j.Log4j2;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        log.info("请选择功能:1->计算价格,2->选品");
        if (scanner.hasNext()) {
            int select = scanner.nextInt();
            switch (select) {
                case 2 -> {
                    ClipboardListener clipboardListener = new ClipboardListener();
                    log.info("选品监听ing");
                }
                default -> {CalculateListener clipboardListener = new CalculateListener();
                    log.info("价格计算监听ing");}

            }

        }
        TimeUnit.SECONDS.sleep(150000);
    }
}
