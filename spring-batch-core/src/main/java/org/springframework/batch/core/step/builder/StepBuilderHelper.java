/*
 * Copyright 2006-2022 the original author or authors.
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
package org.springframework.batch.core.step.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.listener.StepListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepProperties;
import org.springframework.batch.support.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * A base class and utility for other step builders providing access to common properties
 * like job repository and listeners.
 *
 * @author Dave Syer
 * @author Michael Minella
 * @author Mahmoud Ben Hassine
 * @author Taeik Lim
 * @since 2.2
 */
public abstract class StepBuilderHelper<B extends StepBuilderHelper<B>> {

	protected final Log logger = LogFactory.getLog(getClass());

	protected final StepProperties properties;

	public StepBuilderHelper(String name) {
		this.properties = new StepProperties();
		properties.setName(name);
	}

	/**
	 * Create a new builder initialized with any properties in the parent. The parent is
	 * copied, so it can be re-used.
	 * @param parent a parent helper containing common step properties
	 */
	protected StepBuilderHelper(StepBuilderHelper<?> parent) {
		this.properties = new StepProperties(parent.properties);
	}

	public B repository(JobRepository jobRepository) {
		properties.setJobRepository(jobRepository);
		return self();
	}

	public B startLimit(int startLimit) {
		properties.setStartLimit(startLimit);
		return self();
	}

	/**
	 * Registers objects using the annotation based listener configuration.
	 * @param listener the object that has a method configured with listener annotation
	 * @return this for fluent chaining
	 */
	public B listener(Object listener) {
		Set<Method> stepExecutionListenerMethods = new HashSet<>();
		stepExecutionListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), BeforeStep.class));
		stepExecutionListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), AfterStep.class));

		if (stepExecutionListenerMethods.size() > 0) {
			StepListenerFactoryBean factory = new StepListenerFactoryBean();
			factory.setDelegate(listener);
			properties.addStepExecutionListener((StepExecutionListener) factory.getObject());
		}

		return self();
	}

	public B listener(StepExecutionListener listener) {
		properties.addStepExecutionListener(listener);
		return self();
	}

	public B allowStartIfComplete(boolean allowStartIfComplete) {
		properties.setAllowStartIfComplete(allowStartIfComplete);
		return self();
	}

	protected abstract B self();

	protected String getName() {
		return properties.getName();
	}

	protected JobRepository getJobRepository() {
		return properties.getJobRepository();
	}

	protected boolean isAllowStartIfComplete() {
		return properties.getAllowStartIfComplete() != null ? properties.getAllowStartIfComplete() : false;
	}

}
