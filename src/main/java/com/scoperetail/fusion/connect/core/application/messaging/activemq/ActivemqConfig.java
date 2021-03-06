package com.scoperetail.fusion.connect.core.application.messaging.activemq;

/*-
 * *****
 * fusion-connect-core
 * -----
 * Copyright (C) 2018 - 2022 Scope Retail Systems Inc.
 * -----
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * =====
 */

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import com.scoperetail.fusion.connect.core.config.AMQBroker;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class ActivemqConfig implements InitializingBean {
  private FusionConfig fusionConfig;
  private ApplicationContext applicationContext;

  @Override
  public void afterPropertiesSet() throws Exception {
    final BeanDefinitionRegistry registry = getBeanDefinitionRegistry();
    registerConnectionFactories(registry);
  }

  private BeanDefinitionRegistry getBeanDefinitionRegistry() {
    return (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
  }

  private void registerConnectionFactories(final BeanDefinitionRegistry registry)
      throws JMSException {
    boolean isPrimaryBean = true;
    for (final AMQBroker broker : fusionConfig.getAmqBrokers()) {
      final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
      activeMQConnectionFactory.setBrokerURL(broker.getUrl());
      checkAlive(activeMQConnectionFactory, broker);
      final BeanDefinitionBuilder factoryBeanDefinitionBuilder =
          BeanDefinitionBuilder.rootBeanDefinition(CachingConnectionFactory.class)
              .addPropertyValue("targetConnectionFactory", activeMQConnectionFactory)
              .setPrimary(isPrimaryBean);
      registry.registerBeanDefinition(
          broker.getConnectionFactoryName(), factoryBeanDefinitionBuilder.getBeanDefinition());
      isPrimaryBean = false;
      log.info("Registered connection factory with name: {}", broker.getConnectionFactoryName());
    }
  }

  private void checkAlive(final ConnectionFactory connectionFactory, final AMQBroker broker)
      throws JMSException {
    try {
      final Connection connection = connectionFactory.createConnection();
      connection.close();
    } catch (final JMSException e) {
      log.error("Unable to connect to broker: {}", broker);
      throw e;
    }
  }
}
