package org.felix.thesis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class DockerWrapper {
    private static DockerResult _exec(String command, String[] envp, File dir, Integer timeout) throws IOException, InterruptedException {
        //default values for optional envp, timeout and dir
        envp = envp!=null ? envp : new String[0];
        dir = dir!=null ? dir : new File(".");
        timeout = timeout!=null ? timeout : 5;

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(
            command, envp, dir
        );

        DockerResult ret = new DockerResult();
        //proc.getOutputStream().write("");
        BufferedReader outStream = new BufferedReader(
                new InputStreamReader(
                    proc.getInputStream(),
                    StandardCharsets.UTF_8
                )
        );
        BufferedReader errStream = new BufferedReader(
                new InputStreamReader(
                    proc.getErrorStream(),
                    StandardCharsets.UTF_8
                )
        );
        proc.waitFor((long)timeout, TimeUnit.SECONDS);

        String l;
        // get stdOut
        StringBuilder out = new StringBuilder();
        while ((l = outStream.readLine()) != null) {out.append(l);}
        // get stdErr
        StringBuilder err = new StringBuilder();
        while ((l = errStream.readLine()) != null) {err.append(l);}

        ret.stdOut = out.toString();
        ret.stdErr = err.toString();

        ret.exitVal = proc.exitValue();

        return ret;
    }
    private static DockerResult _exec(String command) throws IOException, InterruptedException {
        return DockerWrapper._exec(
                command,
                new String[0],
                new File("."),
                5*1000
        );
    }

    public static DockerResult build(String imageName, Path dockerFilePath) {
        DockerResult result;
        try {
            result = _exec(
                String.format("docker build -q -t %s -f %s .", imageName, dockerFilePath.toString()),
                new String[0],
                new File("../setups/"),
                5*1000
            );
        } catch (IOException e) {
            result = new DockerResult(e, "docker during build");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "timeout during build");
        }
        if (result.stdOut.contains("Error")) {
            result.exitVal = -1;
            result.debugMessage = "stdout contains references to errors";
        }
        return result;
    }

    public static DockerResult remove(String imageName) {
        DockerResult result;
        try {
            result = _exec(
                    String.format("docker rmi %s", imageName)
            );
        } catch (IOException e) {
            result = new DockerResult(e, "IOException during remove");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "timeout during remove");
        }
        return result;
    }

    public static DockerResult run(String imageName, String containerName) {
        return run(imageName, containerName, false, 443);
    }
    public static DockerResult run(String imageName, String containerName, boolean restart, int port) {

        if (isRunning(containerName)) {
            if (restart) {
                DockerResult res = stop(containerName);
                if (res.exitVal != 0) {
                    res.debugMessage = "unable to stop container";
                    return res;
                }
            } else {
                DockerResult r = new DockerResult("", "", -1);
                r.debugMessage = "unable to start docker container because the name is already taken by another container";
                return r;
            }
        }

        DockerResult result;
        try {
            result = _exec(
                    String.format("docker run --rm -d -p %d:443 --name %s %s", port, containerName, imageName)
            );
        } catch (IOException e) {
            result = new DockerResult(e, "IOException during run");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "timeout during run");
        }
        return result;
    }

    public static DockerResult stop(String containerName) {
        DockerResult result;
        try {
            result = _exec(
                    String.format("docker kill %s", containerName)
            );
        } catch (IOException e) {
            result = new DockerResult(e, "IOException during kill");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "timeout during kill");
        }
        return result;
    }

    public static DockerResult execute(String containerName, String command, Integer timeout) {
        timeout = timeout!=null ? timeout : 5;
        DockerResult result;
        try {
            result = _exec(
                    String.format("docker exec %s %s", containerName, command),
                    null,
                    null,
                    timeout
            );
        } catch (IOException e) {
            result = new DockerResult(e, "IOException during execute");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "timeout during execute");
        }
        return result;
    }

    public static boolean imageExists(String imageName) {
        DockerResult result;
        try {
            result = _exec(
                    String.format("docker images -q %s", imageName)
            );
        } catch (IOException e) {
            result = new DockerResult(e, "IOException during imageExists");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "Timeout during imageExists");
        }
        Pattern p = Pattern.compile("[0-9a-fA-F]{12}");
        return p.matcher(result.stdOut).find();
    }

    public static boolean isRunning(String containerName) {
        DockerResult result;
        try {
            result = _exec(
                    String.format("docker ps -f Name=%s -q", containerName)
            );
        } catch (IOException e) {
            result = new DockerResult(e, "IOException during isRunning");
        } catch (InterruptedException e) {
            result = new DockerResult(e, "timeout during isRunning");
        }
        Pattern p = Pattern.compile("[0-9a-fA-F]{12}");
        return p.matcher(result.stdOut).find();
    }

}
