package com.fight;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        log.info("请选择功能:1->计算价格,2->选品");
        if (scanner.hasNext()) {
            String select = scanner.nextLine();
            switch (select) {
                case "2" -> {
                    ClipboardListener clipboardListener = new ClipboardListener();
                    log.info("选品监听ing");
                }
                default -> {
                    CalculateListener clipboardListener = new CalculateListener();
                    log.info("请输入监控dir");
                    String next = scanner.nextLine();
                    CompletableFuture.runAsync(() -> ConvertPng.watchFileStart(StringUtils.isEmpty(next) ? "d:\\新建文件夹" : next));
                    log.info("开始监控转换图片" + next);
                }

            }
            TimeUnit.SECONDS.sleep(150000);
        }
    }
}
