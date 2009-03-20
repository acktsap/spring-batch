/*
 * Copyright 2006-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.configuration.xml;

import java.util.List;

import org.springframework.batch.core.job.flow.FlowJob;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for the lt;job/gt; element in the Batch namespace. Sets up and returns
 * a bean definition for a {@link org.springframework.batch.core.Job}.
 * 
 * @author Dave Syer
 * 
 */
public class JobParser extends AbstractSingleBeanDefinitionParser {

	private JobExecutionListenerParser jobListenerParser = new JobExecutionListenerParser();

	@Override
	protected Class<FlowJob> getBeanClass(Element element) {
		return FlowJob.class;
	}

	/**
	 * Create a bean definition for a
	 * {@link org.springframework.batch.core.job.flow.FlowJob}. The
	 * <code>jobRepository</code> attribute is a reference to a
	 * {@link org.springframework.batch.core.repository.JobRepository} and
	 * defaults to "jobRepository". Nested step elements are delegated to an
	 * {@link InlineStepParser}.
	 * 
	 * @see AbstractSingleBeanDefinitionParser#doParse(Element, ParserContext,
	 *      BeanDefinitionBuilder)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		CoreNamespaceUtils.checkForStepScope(parserContext, parserContext.extractSource(element));

		String jobName = element.getAttribute("id");
		builder.addConstructorArgValue(jobName);

		builder.setAbstract(Boolean.valueOf(element.getAttribute("abstract")));

		String parentRef = element.getAttribute("parent");
		if (StringUtils.hasText(parentRef)) {
			builder.setParentName(parentRef);
		}

		String repositoryAttribute = element.getAttribute("job-repository");
		if (!StringUtils.hasText(repositoryAttribute)) {
			repositoryAttribute = "jobRepository";
		}
		builder.addPropertyReference("jobRepository", repositoryAttribute);

		String restartableAttribute = element.getAttribute("restartable");
		if (StringUtils.hasText(restartableAttribute)) {
			builder.addPropertyValue("restartable", restartableAttribute);
		}

		String incrementer = (element.getAttribute("incrementer"));
		if (StringUtils.hasText(incrementer)) {
			builder.addPropertyReference("jobParametersIncrementer", incrementer);
		}

		FlowParser flowParser = new FlowParser(jobName, repositoryAttribute);
		BeanDefinition flowDef = flowParser.parse(element, parserContext);
		builder.addPropertyValue("flow", flowDef);

		List<Element> listenersElements = DomUtils.getChildElementsByTagName(element, "listeners");
		if (listenersElements.size() == 1) {
			Element listenersElement = listenersElements.get(0);
			CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(listenersElement.getTagName(),
					parserContext.extractSource(element));
			parserContext.pushContainingComponent(compositeDef);
			ManagedList listeners = new ManagedList();
			listeners.setMergeEnabled(Boolean.valueOf(listenersElement.getAttribute("merge")));
			List<Element> listenerElements = (List<Element>) DomUtils.getChildElementsByTagName(listenersElement,
					"listener");
			for (Element listenerElement : listenerElements) {
				listeners.add(jobListenerParser.parse(listenerElement, parserContext));
			}
			builder.addPropertyValue("jobExecutionListeners", listeners);
			parserContext.popAndRegisterContainingComponent();
		}
		else if (listenersElements.size() > 1) {
			parserContext.getReaderContext().error(
					"The 'listeners' element may not appear more than once in a single <job/>.", element);
		}

	}

}
