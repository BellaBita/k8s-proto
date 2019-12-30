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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.AppsV1beta1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.ExtensionsApi;
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
import io.kubernetes.client.openapi.models.V1beta1StatefulSet;
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
public class LenaYaml {

  private static String NAME_SPACE = "default";

  public static void main(String[] args) {

    LenaYaml lenaYaml = new LenaYaml();

    try {
      //lenaYaml.createOrReplace("./menifests/lena-session-configmap.yaml", NAME_SPACE);
      lenaYaml.createOrReplace("./menifests/lena-session-deploy.yaml", NAME_SPACE);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ApiException e) {
      System.out.println(String.format("Error Code : %d, Message: %s, \n Response Body : %s, \n Response Headers : %s",
          e.getCode(), e.getMessage(), e.getResponseBody()));
      e.printStackTrace();
    }

  }

  public Object createOrReplace(String filePath, String namespace) throws IOException, ApiException {
    ApiClient client = buildClient();
    Configuration.setDefaultApiClient(client);
    Object loaded = (Object) Yaml.load(new File(filePath));
    Object created = null;

    if (loaded instanceof V1Deployment) {
      created = this.createDeployment((V1Deployment) loaded, namespace, client);
    } else if (loaded instanceof V1ConfigMap) {
      created = this.createConfigMap((V1ConfigMap) loaded, namespace, client);
    } else if (loaded instanceof V1Service) {
      created = this.createService((V1Service) loaded, namespace, client);
    } else if (loaded instanceof V1StatefulSet) {
      created = this.createStatefulSet((V1StatefulSet) loaded, namespace, client);
    } else {
      throw new ApiException("Not supports object type. : " + loaded.getClass().getName());
    }
    System.out.println("\n# Creation Result==============================");
    System.out.println(Yaml.dump(created));
    System.out.println("===============================================\n");

    return created;
  }

  public V1Deployment createDeployment(V1Deployment loaded, String namespace, ApiClient client)
      throws IOException, ApiException {
    AppsV1Api appsV1Api = new AppsV1Api(client);
    V1Deployment created = null;
    try {
      V1Deployment oldOne = appsV1Api.readNamespacedDeployment(loaded.getMetadata().getName(), namespace, "true", null,
          null);
      if (oldOne !=null) {
        created = appsV1Api.replaceNamespacedDeployment(loaded.getMetadata().getName(), namespace, loaded, "true", null,
          null);
      }
    } catch (ApiException e) {
      System.out.println(String.format("Error code : %d, Message : %s, Response Body : %s", e.getCode(), e.getMessage(),
          e.getResponseBody()));
      created = appsV1Api.createNamespacedDeployment(namespace, loaded, "true", null, null);
    }
    return created;
  }

  public V1StatefulSet createStatefulSet(V1StatefulSet loaded, String namespace, ApiClient client)
      throws IOException, ApiException {
    AppsV1Api appsV1Api = new AppsV1Api(client);
    V1StatefulSet created = null;
    try {
      V1StatefulSet oldOne = appsV1Api.readNamespacedStatefulSet(loaded.getMetadata().getName(), namespace, "true", null,
          null);
      if (oldOne !=null) {
        created = appsV1Api.replaceNamespacedStatefulSet(loaded.getMetadata().getName(), namespace, loaded, "true", null,
          null);
      }
    } catch (ApiException e) {
      System.out.println(String.format("Error code : %d, Message : %s, Response Body : %s", e.getCode(), e.getMessage(),
          e.getResponseBody()));
      created = appsV1Api.createNamespacedStatefulSet(namespace, loaded, "true", null, null); 
    }
    return created;
  }

  public V1ConfigMap createConfigMap(V1ConfigMap loaded, String namespace, ApiClient client)
      throws IOException, ApiException {
    CoreV1Api coreV1Api = new CoreV1Api(client);
    V1ConfigMap created = null;
    try {
      V1ConfigMap oldOne = coreV1Api.readNamespacedConfigMap(loaded.getMetadata().getName(), namespace, "true", null,
          null);
      if (oldOne !=null) {
        created = coreV1Api.replaceNamespacedConfigMap(loaded.getMetadata().getName(), namespace, loaded, "true", null,
          null);
      }
    } catch (ApiException e) {
      System.out.println(String.format("Error code : %d, Message : %s, Response Body : %s", e.getCode(), e.getMessage(),
          e.getResponseBody()));
      created = coreV1Api.createNamespacedConfigMap(namespace, loaded, "true", null, null);
    }
    return created;
}

  public V1Service createService(V1Service loaded, String namespace, ApiClient client)
      throws IOException, ApiException {
    CoreV1Api coreV1Api = new CoreV1Api(client);
    V1Service created = null;
    try {
      V1Service oldOne = coreV1Api.readNamespacedService(loaded.getMetadata().getName(), namespace, "true", null,
    null);
      if (oldOne !=null) {
        created = coreV1Api.replaceNamespacedService(loaded.getMetadata().getName(), namespace, loaded, "true", null,
          null);
      }
    } catch (ApiException e) {
      System.out.println(String.format("Error code : %d, Message : %s, Response Body : %s", e.getCode(), e.getMessage(),
          e.getResponseBody()));
          created = coreV1Api.createNamespacedService(namespace, loaded, "true", null, null);
    }
    return created;
  }

  public ApiClient buildClient() throws IOException {
    return buildClient(System.getProperty("user.home") + "/.kube/config");
  }

  public ApiClient buildClient(String kubeConfigPath) throws IOException {
    ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    return client;
  }
}