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
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.step.builder.TaskletStepBuilder
import org.springframework.batch.item.ItemStream
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.exception.ExceptionHandler
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.interceptor.TransactionAttribute

/**
 * @author Taeik Lim
 *
 */
@BatchDslMarker
class TaskletStepBuilderDsl internal constructor(
    private val taskletStepBuilder: TaskletStepBuilder
) {

    fun exceptionHandler(exceptionHandler: ExceptionHandler) {
        taskletStepBuilder.exceptionHandler(exceptionHandler)
    }

    fun transactionManager(transactionManager: PlatformTransactionManager) {
        taskletStepBuilder.transactionManager(transactionManager)
    }

    fun transactionAttribute(transactionAttribute: TransactionAttribute) {
        taskletStepBuilder.transactionAttribute(transactionAttribute)
    }

    fun listener(chunkListener: ChunkListener) {
        taskletStepBuilder.listener(chunkListener)
    }

    fun listener(listener: Any) {
        taskletStepBuilder.listener(listener)
    }

    fun stream(stream: ItemStream) {
        taskletStepBuilder.stream(stream)
    }

    fun stepOperations(repeatOperations: RepeatOperations) {
        taskletStepBuilder.stepOperations(repeatOperations)
    }

    internal fun build(): Step = taskletStepBuilder.build()
}
