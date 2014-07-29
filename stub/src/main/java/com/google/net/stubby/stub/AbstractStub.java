package com.google.net.stubby.stub;

import com.google.common.collect.Maps;
import com.google.net.stubby.Channel;
import com.google.net.stubby.MethodDescriptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

/**
 * Common base type for stub implementations. Allows for reconfiguration.
 */
// TODO(user): Move into 3rd party when tidy
// TOOD(lryan/kevinb): Excessive parameterization can be a pain, try to eliminate once the generated
// code is more tangible.
public abstract class AbstractStub<S extends AbstractStub, C extends AbstractServiceDescriptor<C>> {
  protected final Channel channel;
  protected final C config;

  /**
   * Constructor for use by subclasses.
   */
  protected AbstractStub(Channel channel, C config) {
    this.channel = channel;
    this.config = config;
  }

  protected C getServiceDescriptor() {
    return config;
  }

  public StubConfigBuilder configureNewStub() {
    return new StubConfigBuilder();
  }

  /**
   * Returns a new stub configuration for the provided method configurations.
   */
  protected abstract S build(Channel channel, C config);

  /**
   * Utility class for (re) configuring the operations in a stub.
   */
  public class StubConfigBuilder {

    private final Map<String, MethodDescriptor> methodMap;

    private StubConfigBuilder() {
      methodMap = Maps.newHashMapWithExpectedSize(config.methods().size());
      for (MethodDescriptor method : AbstractStub.this.config.methods()) {
        methodMap.put(method.getName(), method);
      }
    }

    /**
     * Set a header provider for all methods in the stub.
     */
    public StubConfigBuilder setHeader(String headerName, Provider<String> headerValueProvider) {
      for (Map.Entry<String, MethodDescriptor> entry : methodMap.entrySet()) {
        entry.setValue(entry.getValue().withHeader(headerName, headerValueProvider));
      }
      return this;
    }

    /**
     * Set a timeout for all methods in the stub.
     */
    public StubConfigBuilder setTimeout(long timeout, TimeUnit unit) {
      for (Map.Entry<String, MethodDescriptor> entry : methodMap.entrySet()) {
        entry.setValue(entry.getValue().withTimeout(timeout, unit));
      }
      return this;
    }

    /**
     * Create a new stub configuration
     */
    public S build() {
      return AbstractStub.this.build(channel, config.build(methodMap));
    }
  }
}