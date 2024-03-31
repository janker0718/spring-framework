/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.context.annotation;

import java.util.Arrays;
import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 独立应用程序上下文，接受<em>组件类</em>作为输入 -
 *   特别是 {@link Configuration @Configuration} 带注释的类，但也是简单的
 *   使用 {@code jakarta.inject} 注解的 {@link org.springframework.stereotype.Component @Component} 类型和 JSR-330 兼容类。
 *
 * <p>允许使用 {@link #register(Class...)} 逐一注册类，以及使用 {@link #scan(String...)} 进行类路径扫描。
 *
 * <p>如果有多个 {@code @Configuration} 类，后面的类中定义的
 * {@link Bean @Bean} 方法将覆盖前面的类中定义的方法。
 * 可以利用这一点通过额外的 {@code @Configuration} 类故意覆盖某些 bean 定义。
 *
 * <p>See {@link Configuration @Configuration}'s javadoc for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	private final AnnotatedBeanDefinitionReader reader;

	/**
	 * 类路径bean定义扫描器
	 */
	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * 创建一个需要填充的新 AnnotationConfigApplicationContext
	 * 通过调用注册方法 {@link #register}  然后手动刷新 {@linkplain #refresh}.
	 */
	public AnnotationConfigApplicationContext() {
		//获取应用启动类 进行启动
		StartupStep createAnnotatedBeanDefReader = getApplicationStartup().start("spring.context.annotated-bean-reader.create");
		//初始化一个注解bean定义读取器
		this.reader = new AnnotatedBeanDefinitionReader(this);
		//创建注解式的bean定义读取器结束
		createAnnotatedBeanDefReader.end();
		//初始化一个类路径bena定义扫描器
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 使用给定的 DefaultListableBeanFactory 创建一个新的 AnnotationConfigApplicationContext。
	 * @param beanFactory 用于此上下文的 DefaultListableBeanFactory 实例
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
		//初始化一个注解bean定义读取器
		this.reader = new AnnotatedBeanDefinitionReader(this);
		//初始化一个类路径bena定义扫描器
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 创建一个新的 AnnotationConfigApplicationContext，从给定的组件类派生 bean 定义并自动刷新上下文。
	 * @param componentClasses 一个或多个组件类—— 例如，{@link Configuration @Configuration} 类
	 */
	public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
		//执行默认无参构造函数
		this();
		//注册组件类
		register(componentClasses);
		//手动刷新
		refresh();
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, scanning for components
	 * in the given packages, registering bean definitions for those components,
	 * and automatically refreshing the context.
	 * @param basePackages the packages to scan for component classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		//扫描基础包
		scan(basePackages);
		//刷新
		refresh();
	}


	/**
	 * Propagate the given custom {@code Environment} to the underlying
	 * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * 提供自定义的 {@link BeanNameGenerator} 以与 {@link AnnotatedBeanDefinitionReader} 和/或 {@link ClassPathBeanDefinitionScanner}（如果有）一起使用。
	 * <p>默认为{@link AnnotationBeanNameGenerator}。
	 * <p>对此方法的任何调用都必须在调用 {@link #register(Class...)} 和/或 {@link #scan(String...)} 之前进行。
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}


	/**
	 * 将{@link ScopeMetadataResolver} 设置用于注册组件类。
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// AnnotationConfigRegistry的实现
	//---------------------------------------------------------------------

	/**
	 * 注册一个或多个要处理的组件类。
	 * <p>请注意，必须调用 {@link #refresh()} 才能使上下文完全处理新类。
	 * @param componentClasses 一个或多个组件类—— 例如，{@link Configuration @Configuration} 类
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	@Override
	public void register(Class<?>... componentClasses) {
		//断言组件类不能为空
		Assert.notEmpty(componentClasses, "At least one component class must be specified");
		//启动步骤 - 注册组件类
		StartupStep registerComponentClass = getApplicationStartup().start("spring.context.component-classes.register")
				.tag("classes", () -> Arrays.toString(componentClasses));
		//读取器注册组件类
		this.reader.register(componentClasses);
		//注册组件类步骤结束
		registerComponentClass.end();
	}

	/**
	 * 在指定的基础包中执行扫描。
	 * <p>请注意，必须调用 {@link #refresh()} 才能使上下文完全处理新类。
	 * @param basePackages 用于扫描组件类的包
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	@Override
	public void scan(String... basePackages) {
		//断言 基础包不能为空
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		//启动步骤标识
		StartupStep scanPackages = getApplicationStartup().start("spring.context.base-packages.scan")
				.tag("packages", () -> Arrays.toString(basePackages));
		//扫描器 扫描基础包
		this.scanner.scan(basePackages);
		//包扫描结束后动作、可重写 加入自己需要执行的逻辑
		scanPackages.end();
	}


	//---------------------------------------------------------------------
	// 将超类 registerBean 调用调整为 AnnotatedBeanDefinitionReader
	//---------------------------------------------------------------------

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		this.reader.registerBean(beanClass, beanName, supplier, customizers);
	}

}
