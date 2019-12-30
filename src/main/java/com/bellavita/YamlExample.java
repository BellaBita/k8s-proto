/*
Copyright 2018 The Kubernetes Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.bellavita;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentBuilder;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceBuilder;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;

/**
 * A simple example of how to parse a Kubernetes object.
 *
 * <p>
 * Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.YamlExample"
 *
 * <p>
 * From inside $REPO_DIR/examples
 */
public class YamlExample {
  public static void main(String[] args) throws IOException, ApiException, ClassNotFoundException {
    V1Pod pod = new V1PodBuilder().withNewMetadata().withName("apod").endMetadata().withNewSpec().addNewContainer()
        .withName("www2").withImage("nginx").withNewResources().withLimits(new HashMap<String, Quantity>())
        .endResources().endContainer().endSpec().build();
    // System.out.println("Pod-apod Yaml============================");
    // System.out.println(Yaml.dump(pod));
    // System.out.println("=========================================");
    // System.out.println("");

    Map<String, String> selector = new HashMap<String, String>();
    selector.put("app", "apod");
    V1Service svc = new V1ServiceBuilder().withNewMetadata().withName("aservice").endMetadata().withNewSpec()
        // .withSessionAffinity("ClusterIP")
        .withSelector(selector).withType("NodePort").addNewPort().withProtocol("TCP").withName("client").withPort(8008)
        .withNodePort(8080).withTargetPort(new IntOrString(8080)).endPort().endSpec().build();
    // System.out.println("Service-aservice Yaml====================");
    // System.out.println(Yaml.dump(svc));
    // System.out.println("=========================================");
    // System.out.println("");

    // Read yaml configuration file, and deploy it
    // ApiClient client = Config.defaultClient();
    ApiClient client = buildClient(System.getProperty("user.home") + "/.kube/config");
    Configuration.setDefaultApiClient(client);

    CoreV1Api coreV1Api = new CoreV1Api(client);

    // V1Status deletedPodStatus = coreV1Api.deleteNamespacedPod("apod", "default",
    // "true", null, null, null, null, null);
    // System.out.println("delete-pod-result : apod Yaml====================");
    // System.out.println(Yaml.dump(deletedPodStatus));
    // System.out.println("=========================================");
    // System.out.println("");

    // V1Pod createdPod = coreV1Api.createNamespacedPod("default", pod, "true",
    // null, null);
    // System.out.println("Create-pod-result : apod Yaml====================");
    // System.out.println(Yaml.dump(createdPod));
    // System.out.println("=========================================");
    // System.out.println("");

    // V1Service createdService = coreV1Api.createNamespacedService("default", svc,
    // "true", null, null);
    // System.out.println("Create-svc-result : aservice Yaml====================");
    // System.out.println(Yaml.dump(createdService));
    // System.out.println("=========================================");
    // System.out.println("");

    V1NamespaceList namespaceList = coreV1Api.listNamespace("true", null, null, null, null, null, null, null, null);

    for (V1Namespace n : namespaceList.getItems()) {
      System.out.println(String.format("kind:%s, name:%s", n.getKind(), n.getMetadata().getName()));
      // kube-public, kube-system, kube-node-lease, ingress-nginx, cattel-system
      // (rancher)
    }

    // See issue #474. Not needed at most cases, but it is needed if you are using
    // war
    // packging or running this on JUnit.
    Yaml.addModelMap("v1", "Service", V1Service.class);
    Yaml.addModelMap("v1", "StatefulSet", V1StatefulSet.class);
    Yaml.addModelMap("v1", "Deployment", V1Deployment.class);
    Yaml.addModelMap("v1", "ConfigMap", V1ConfigMap.class);

    // Example yaml file can be found in $REPO_DIR/test-svc.yaml
    // File file = new File("test-svc.yaml");
    // V1Service yamlSvc = (V1Service) Yaml.load(file);

    // // Deployment and StatefulSet is defined in apps/v1, so you should use AppsV1Api instead of
    // // CoreV1API
    // CoreV1Api api = new CoreV1Api();
    // V1Service createResult = api.createNamespacedService("default", yamlSvc, null, null, null);

    // System.out.println(createResult);

    // V1Status deleteResult =
    //     api.deleteNamespacedService(
    //         yamlSvc.getMetadata().getName(),
    //         "default",
    //         null,
    //         null,
    //         null,
    //         null,
    //         null,
    //         new V1DeleteOptions());
    // System.out.println(deleteResult);
  }

  public static ApiClient buildClient(String kubeConfigPath) throws IOException {
    ApiClient client =
        ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    return client;
  }
}