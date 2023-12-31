/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.core.metrics.jfr;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;

/**
 * Java Flight Recorder 的 {@link ApplicationStartup} 实现。
 * <p>此变体将 {@link StartupStep} 记录为 Flight Recorder 事件。
 * 由于此类事件仅支持基本类型，
 * 因此 {@link org.springframework.core.metrics.StartupStep.Tags} 被序列化为单个字符串属性。
 * <p>在应用程序上下文中配置此功能后，您可以通过启动启用记录的应用程序来记录数据：
 * 使用以下启动参数可开启此功能。
 * {@code java -XX:StartFlightRecording:filename=recording.jfr,duration=10s -jar app.jar}.
 *
 * @author Brian Clozel
 * @since 5.3
 */
public class FlightRecorderApplicationStartup implements ApplicationStartup {

	private final AtomicLong currentSequenceId = new AtomicLong();

	private final Deque<Long> currentSteps;


	public FlightRecorderApplicationStartup() {
		this.currentSteps = new ConcurrentLinkedDeque<>();
		this.currentSteps.offerFirst(this.currentSequenceId.get());
	}


	@Override
	public StartupStep start(String name) {
		long sequenceId = this.currentSequenceId.incrementAndGet();
		this.currentSteps.offerFirst(sequenceId);
		return new FlightRecorderStartupStep(sequenceId, name,
				this.currentSteps.getFirst(), committedStep -> this.currentSteps.removeFirstOccurrence(sequenceId));
	}

}
