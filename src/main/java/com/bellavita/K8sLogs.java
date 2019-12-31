package com.bellavita;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

/**
 * A simple example of how to use the Java API
 *
 * <p>
 * Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.LogsExample"
 *
 * <p>
 * From inside $REPO_DIR/examples
 */
public class K8sLogs {
  public static void main(String[] args) throws IOException, InterruptedException {
    K8sLogs logs = new K8sLogs();
    try {
      logs.streamPodLog("default", "lena-session-0", System.out);
    } catch (ApiException e) {
      K8sUtils.printApiException(e);
    }
  }

  public void streamPodLog(String namespace, String podName, OutputStream out)
      throws IOException, ApiException, InterruptedException {

    ApiClient client = K8sUtils.buildApiClient();
    CoreV1Api coreApi = new CoreV1Api(client);
    PodLogs logs = new PodLogs(client);
    V1Pod pod = coreApi.readNamespacedPod(podName, namespace, "false", null,
    null);
    
    if (pod != null) {
      //K8sUtils.print(pod);
      InputStream is = logs.streamNamespacedPodLog(pod);
      try {
          ByteStreams.copy(is, out);
        } catch (SocketTimeoutException e) {
          System.out.println(e.getMessage());
        }
    } else {
      throw new ApiException(404, String.format("Pod '%s' not found in namespace '%s'", podName, namespace));
    }
  }

  public static void main2(String[] args) throws IOException, ApiException, InterruptedException {
    ApiClient client = K8sUtils.buildApiClient();
    Configuration.setDefaultApiClient(client);
    CoreV1Api coreApi = new CoreV1Api(client);

    PodLogs logs = new PodLogs();
    V1Pod pod = coreApi.listNamespacedPod("default", "false", null, null, null, null, null, null, null, null).getItems()
        .get(0);
    K8sUtils.print(pod);
    InputStream is = logs.streamNamespacedPodLog(pod);
    ByteStreams.copy(is, System.out);
  }
}