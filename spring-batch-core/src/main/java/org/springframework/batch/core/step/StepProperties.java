/*
 * Copyright 2006-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.step;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;

/**
 * A common properties for {@link Step}.
 *
 * @author Taeik Lim
 */
public class StepProperties {

	private List<StepExecutionListener> stepExecutionListeners = new ArrayList<>();

	private int startLimit = Integer.MAX_VALUE;

	private Boolean allowStartIfComplete;

	private JobRepository jobRepository;

	public StepProperties() {
	}

	public StepProperties(StepProperties properties) {
		this.name = properties.name;
		this.startLimit = properties.startLimit;
		this.allowStartIfComplete = properties.allowStartIfComplete;
		this.jobRepository = properties.jobRepository;
		this.stepExecutionListeners = new ArrayList<>(properties.stepExecutionListeners);
	}

	public JobRepository getJobRepository() {
		return jobRepository;
	}

	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<StepExecutionListener> getStepExecutionListeners() {
		return stepExecutionListeners;
	}

	public void addStepExecutionListeners(List<StepExecutionListener> stepExecutionListeners) {
		this.stepExecutionListeners.addAll(stepExecutionListeners);
	}

	public void addStepExecutionListener(StepExecutionListener stepExecutionListener) {
		this.stepExecutionListeners.add(stepExecutionListener);
	}

	public Integer getStartLimit() {
		return startLimit;
	}

	public void setStartLimit(Integer startLimit) {
		this.startLimit = startLimit;
	}

	public Boolean getAllowStartIfComplete() {
		return allowStartIfComplete;
	}

	public void setAllowStartIfComplete(Boolean allowStartIfComplete) {
		this.allowStartIfComplete = allowStartIfComplete;
	}

	private String name;

}
