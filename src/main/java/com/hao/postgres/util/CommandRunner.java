package com.hao.postgres.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandRunner {

    @Value("#{'${spring.datasource.url}'.substring(5)}")
    String url;

    @Value("${spring.datasource.username}")
    String username;

    @Value("${spring.datasource.password}")
    String password;

    private static final int POSITION = 13;

    private String fqdn() {
        return url.substring(0, POSITION) + username + ":" + password + "@" + url.substring(POSITION);
    }

    String psqlCommand(String cli) {
        String command = String.format("psql %s -c '%s'", fqdn(), cli);
        log.info("Full psql command: {}", command);
        log.info("\n\n============= {} =============\n", cli);
        return command;
    }

    public String psql(String cli) {
        return execute(psqlCommand(cli));
    }


    @SneakyThrows
    public String execute(String command) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process = builder.start();
        StringBuffer sb = new StringBuffer();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(),
                (line) -> {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                    System.out.println(line);
                });
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        String errorMessage = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
        assert exitCode == 0 : "[" + errorMessage + "]";
        return sb.toString();
    }


    // UTIL FUNCTIONS:


    public void describeTable(String tblName) {
        psql("\\d " + tblName);
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

}