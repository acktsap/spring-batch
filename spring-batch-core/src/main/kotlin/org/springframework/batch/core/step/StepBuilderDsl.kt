/*
 * Copyright 2021-2021 the original author or authors.
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

package org.springframework.batch.core.step

import org.springframework.batch.core.BatchDslMarker
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.beans.factory.BeanFactory
import kotlin.reflect.KClass

/**
 * @author Taeik Lim
 *
 */
@BatchDslMarker
class StepBuilderDsl internal constructor(
    private val beanFactory: BeanFactory,
    private val stepBuilder: StepBuilder
) {
    fun repository(jobRepository: JobRepository) {
        stepBuilder.repository(jobRepository)
    }

    fun startLimit(startLimit: Int) {
        stepBuilder.startLimit(startLimit)
    }

    fun listener(listener: Any) {
        stepBuilder.listener(listener)
    }

    fun listener(listener: StepExecutionListener) {
        stepBuilder.listener(listener)
    }

    fun allowStartIfComplete(allowStartIfComplete: Boolean) {
        stepBuilder.allowStartIfComplete(allowStartIfComplete)
    }

    fun tasklet(name: String, init: TaskletStepBuilderDsl.() -> Unit = {}): Step {
        val tasklet = beanFactory.getBean(name, Tasklet::class.java)
        return tasklet(tasklet, init)
    }

    // to use lambda in tasklet
    fun tasklet(tasklet: Tasklet): Step = tasklet(tasklet) {}

    fun tasklet(tasklet: Tasklet, init: TaskletStepBuilderDsl.() -> Unit): Step {
        return TaskletStepBuilderDsl(stepBuilder.tasklet(tasklet)).apply(init).build()
    }
}
