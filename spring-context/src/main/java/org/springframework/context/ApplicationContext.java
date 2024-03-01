/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

/**
 * 为应用程序提供配置的中央接口。
 * 在应用程序运行时这是只读的， 但可能会如果实现支持这一点，则重新加载。
 *
 * <p>ApplicationContext 提供：
 * <ul>
 * <li>用于访问应用程序组件的 Bean 工厂方法。
 * 继承自{@link org.springframework.beans.factory.ListableBeanFactory}。
 * <li>以通用方式加载文件资源的能力。
 * 继承自 {@link org.springframework.core.io.ResourceLoader} 接口。
 * <li>能够向注册的监听器发布事件。
 * 继承自 {@link ApplicationEventPublisher} 接口。
 * <li>解析消息的能力，支持国际化。
 * 继承自 {@link MessageSource} 接口。
 * <li>从父上下文继承。 后代上下文中的定义
 *  将永远优先。 例如，这意味着单亲
 *   context 可以被整个 Web 应用程序使用，而每个 servlet 都有
 * 它自己的子上下文独立于任何其他 servlet。
 * </ul>
 *
 * <p>除了标准的{@link org.springframework.beans.factory.BeanFactory}
 *   生命周期功能，ApplicationContext 实现检测和调用
 *    {@link ApplicationContextAware} bean 以及 {@link ResourceLoaderAware}，
 *   {@link ApplicationEventPublisherAware} 和 {@link MessageSourceAware} bean。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * 返回此应用程序上下文的唯一 ID。
	 * @return 上下文的唯一 id，如果没有则返回 {@code null}
	 */
	@Nullable
	String getId();

	/**
	 * 返回此上下文所属的已部署应用程序的名称。
	 * @return 已部署应用程序的名称，或默认为空字符串
	 */
	String getApplicationName();

	/**
	 * 返回此上下文的友好名称。
	 * @return a display name for this context (never {@code null})
	 */
	String getDisplayName();

	/**
	 * 返回首次加载此上下文时的时间戳。
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * 返回父上下文，如果没有父上下文，则返回 {@code null}
	 * 这是上下文层次结构的根。
	 * @return the parent context, or {@code null} if there is no parent
	 */
	@Nullable
	ApplicationContext getParent();

	/**
	 * 为此上下文公开 AutowireCapableBeanFactory 功能。 <p>除了用于初始化位于应用程序上下文之外的 bean 实例之外，应用程序代码通常不会使用它，
	 * 将 Spring bean 生命周期（全部或部分）应用于它们。
	 * <p>或者，由 BeanFactory 暴露的内部 BeanFactory
	 * {@link ConfigurableApplicationContext} 接口提供对
	 *   {@link AutowireCapableBeanFactory} 接口也是如此。 目前的方法主要
	 *   作为 ApplicationContext 接口上的一个方便的、特定的工具。
	 * <p><b>注意：从 4.2 开始，此方法将始终抛出 IllegalStateException
	 *   在应用程序上下文关闭后。</b> 在当前的 Spring 框架中
	 *   版本，只有可刷新的应用程序上下文才会有这种行为； 从 4.2 开始，
	 *   所有应用程序上下文实现都需要遵守。
	 *   @return 此上下文的 AutowireCapableBeanFactory
	 *   如果上下文不支持@抛出 IllegalStateException
	 *   {@link AutowireCapableBeanFactory}接口，或者不保留
	 *   具有自动装配功能的 bean 工厂（例如，如果 {@code refresh()} 有
	 *   从未被调用），或者如果上下文已经关闭
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
