package com.bellavita;

import java.io.FileReader;
import java.io.IOException;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;

public class K8sUtils {

    public static ApiClient buildApiClient() throws IOException {
        return buildApiClient(System.getProperty("user.home") + "/.kube/config");
    }

    public static ApiClient buildApiClient(String kubeConfigPath) throws IOException {
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        return client;
    }

    public static void printApiException(ApiException e) {
        System.out
                .println(String.format("Error Code : %d, Message: %s, \n Response Body : %s, \n Response Headers : %s",
                        e.getCode(), e.getMessage(), e.getResponseBody()));
        e.printStackTrace();
    }

    public static void print(Object object) {
        System.out.println("= Dump k8s object =======================");
        System.out.println(Yaml.dump(object));
        System.out.println("=========================================");
    }

}