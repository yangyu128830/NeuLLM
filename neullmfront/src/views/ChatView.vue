<template>
  <div
    :class="[
      'chat-page',
      {
        'chat-page--embedded': embedded,
        'chat-page--teacher-shell': embedded && teacherMode,
      },
    ]"
  >
  <div class="app-container">
    <header v-if="isTeacherShell" class="t-agent-hero">
      <div class="t-agent-hero__glow" aria-hidden="true"></div>
      <div class="t-agent-hero__top">
        <div class="t-agent-hero__brand">
          <span class="t-agent-hero__icon"><i class="fas fa-robot"></i></span>
          <div class="t-agent-hero__copy">
            <span class="t-agent-hero__eyebrow">主工作台</span>
            <h1>教学 Agent</h1>
            <p>布置作业、批改建议、学情分析、催交提醒 — 一句话完成</p>
          </div>
        </div>
        <div class="t-agent-hero__meta">
          <span class="t-agent-hero__live">
            <span class="t-agent-hero__live-dot" aria-hidden="true"></span>
            在线
          </span>
        </div>
      </div>
      <div class="t-agent-hero__chips" aria-label="快捷指令">
        <span class="t-agent-hero__chips-label">快捷操作</span>
        <div class="t-agent-hero__chips-row">
          <button
            v-for="(tpl, i) in promptTemplates"
            :key="i"
            type="button"
            class="t-agent-chip"
            @click="applyTemplate(tpl)"
          >
            <span class="t-agent-chip__icon"><i :class="templateIcon(tpl)"></i></span>
            <span class="t-agent-chip__text">{{ tpl.label }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 学生端页头 -->
    <header v-else-if="!teacherMode" class="s-agent-hero">
      <div class="s-agent-hero__glow" aria-hidden="true"></div>
      <div class="s-agent-hero__top">
        <div class="s-agent-hero__brand">
          <span class="s-agent-hero__icon"><i class="fas fa-graduation-cap"></i></span>
          <div class="s-agent-hero__copy">
            <span class="s-agent-hero__eyebrow">AI 学习助手</span>
            <h1>智学伴</h1>
            <p>多智能体协作 · 个性化辅导 · 作业提醒与学情洞察</p>
          </div>
        </div>
        <div v-if="!embedded" class="s-agent-hero__meta">
          <span class="s-agent-hero__live">
            <span class="s-agent-hero__live-dot" aria-hidden="true"></span>
            在线
          </span>
          <nav class="s-agent-hero__nav" aria-label="主导航">
            <router-link to="/messages" class="s-nav-pill s-nav-pill--msg">
              <i class="fas fa-bell"></i>
              <span>消息</span>
              <span v-if="studentUnread > 0" class="chat-nav-badge">{{
                studentUnread > 99 ? '99+' : studentUnread
              }}</span>
            </router-link>
            <router-link to="/assignments" class="s-nav-pill">
              <i class="fas fa-book"></i>
              <span>我的作业</span>
            </router-link>
            <router-link to="/profile" class="s-nav-pill">
              <i class="fas fa-user"></i>
              <span>个人中心</span>
            </router-link>
            <button type="button" class="s-nav-pill s-nav-pill--logout" @click="handleLogout">
              <i class="fas fa-sign-out-alt"></i>
              <span>退出</span>
            </button>
          </nav>
        </div>
      </div>
      <div class="s-agent-hero__chips" aria-label="快捷指令">
        <span class="s-agent-hero__chips-label">试试</span>
        <div class="s-agent-hero__chips-row">
          <button
            v-for="(tpl, i) in promptTemplates"
            :key="i"
            type="button"
            class="s-agent-chip"
            @click="applyTemplate(tpl)"
          >
            <span class="s-agent-chip__icon"><i :class="templateIcon(tpl)"></i></span>
            <span class="s-agent-chip__text">{{ tpl.label }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 教师端独立页头 -->
    <header v-else>
      <div class="header-row">
        <h1><i class="fas fa-robot"></i> 教学 Agent</h1>
        <nav v-if="!embedded" class="user-nav">
          <router-link to="/teacher/progress" class="nav-link-dash">
            <i class="fas fa-arrow-left"></i> 管理后台
          </router-link>
          <button type="button" class="nav-btn" @click="handleLogout">退出</button>
        </nav>
      </div>
      <p class="role-badge">教师端 · 教学 Agent</p>
      <p class="subtitle">布置作业、批改建议、学情分析、催交提醒 — 一句话完成</p>
      <div class="template-bar" aria-label="提示词模板">
        <span class="template-bar-label">试试：</span>
        <button
          v-for="(tpl, i) in promptTemplates"
          :key="i"
          type="button"
          class="template-chip"
          @click="applyTemplate(tpl)"
        >
          {{ tpl.label }}
        </button>
      </div>
    </header>

    <div class="app-content">
      <!-- 聊天容器 -->
      <div class="chat-container" :class="{ 'chat-container--student': !isTeacherShell && !teacherMode }">
        <div v-if="!isTeacherShell && !teacherMode" class="s-chat-mobile-bar">
          <div class="s-chat-mobile-bar__brand">
            <span class="s-chat-mobile-bar__icon"><i class="fas fa-graduation-cap"></i></span>
            <span class="s-chat-mobile-bar__title">智学伴</span>
          </div>
          <span class="s-chat-mobile-bar__live">
            <span class="s-chat-mobile-bar__dot" aria-hidden="true"></span>
            在线
          </span>
        </div>

        <div v-if="isTeacherShell" class="t-chat-mobile-bar">
          <div class="t-chat-mobile-bar__brand">
            <span class="t-chat-mobile-bar__icon"><i class="fas fa-robot"></i></span>
            <div class="t-chat-mobile-bar__copy">
              <span class="t-chat-mobile-bar__title">教学 Agent</span>
              <span class="t-chat-mobile-bar__sub">布置 · 批改 · 学情</span>
            </div>
          </div>
          <span class="t-chat-mobile-bar__live">
            <span class="t-chat-mobile-bar__dot" aria-hidden="true"></span>
            在线
          </span>
        </div>

        <div v-if="isTeacherShell" class="t-agent-panel-head">
          <span class="t-agent-panel-head__title">
            <i class="fas fa-comments"></i>
            {{ chatHeaderTitle }}
          </span>
          <span class="t-agent-panel-head__hint">Enter 发送</span>
        </div>
        <div v-else-if="teacherMode" class="chat-header">
          <i class="fas fa-comments"></i>
          <span>{{ chatHeaderTitle }}</span>
        </div>

        <!-- 聊天消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="showTeacherWelcome" class="t-welcome-panel">
            <div class="t-welcome-panel__hero" aria-hidden="true">
              <div class="t-welcome-panel__hero-glow"></div>
            </div>
            <div class="t-welcome-panel__body">
              <span class="t-welcome-panel__icon"><i class="fas fa-robot"></i></span>
              <p class="t-welcome-panel__eyebrow">教学 Agent · 在线</p>
              <h2 class="t-welcome-panel__title">您好，我是教学助手</h2>
              <p class="t-welcome-panel__desc">{{ teacherWelcomeIntro }}</p>
              <div class="t-welcome-panel__grid" aria-label="快捷能力">
                <button
                  v-for="(cap, i) in teacherWelcomeCapabilities"
                  :key="'tcap-' + i"
                  type="button"
                  class="t-welcome-cap"
                  @click="applyTemplate(cap.tpl)"
                >
                  <span class="t-welcome-cap__icon"><i :class="cap.icon"></i></span>
                  <span class="t-welcome-cap__label">{{ cap.label }}</span>
                </button>
              </div>
            </div>
          </div>

          <div v-if="showStudentWelcome" class="s-welcome-panel">
            <div class="s-welcome-panel__glow" aria-hidden="true"></div>
            <div class="s-welcome-panel__shine" aria-hidden="true"></div>

            <div class="s-welcome-panel__head">
              <div class="s-welcome-panel__brand">
                <span class="s-welcome-panel__icon">
                  <i class="fas fa-robot"></i>
                  <span class="s-welcome-panel__icon-ring" aria-hidden="true"></span>
                </span>
                <div class="s-welcome-panel__copy">
                  <span class="s-welcome-panel__eyebrow">AI 学习助手</span>
                  <h2 class="s-welcome-panel__title">你好，我是智学伴</h2>
                  <p class="s-welcome-panel__desc">{{ welcomeShortText }}</p>
                </div>
              </div>
              <span class="s-welcome-panel__badge">
                <span class="s-welcome-panel__badge-dot" aria-hidden="true"></span>
                随时在线
              </span>
            </div>

            <p class="s-welcome-panel__intro">{{ welcomeIntroText }}</p>

            <div class="s-welcome-panel__grid" aria-label="快捷能力">
              <button
                v-for="(cap, i) in welcomeCapabilities"
                :key="'cap-' + i"
                type="button"
                class="s-welcome-cap"
                @click="applyTemplate(cap.tpl)"
              >
                <span class="s-welcome-cap__icon"><i :class="cap.icon"></i></span>
                <span class="s-welcome-cap__body">
                  <strong>{{ cap.label }}</strong>
                  <small>{{ cap.hint }}</small>
                </span>
                <i class="fas fa-chevron-right s-welcome-cap__arrow" aria-hidden="true"></i>
              </button>
            </div>

            <div class="s-welcome-panel__footer">
              <span><i class="fas fa-shield-alt"></i> 隐私保护</span>
              <span><i class="fas fa-bolt"></i> 秒级响应</span>
              <span><i class="fas fa-layer-group"></i> 多 Agent 协作</span>
            </div>
          </div>

          <div v-for="(message, index) in messages" :key="index"
               v-show="!((showStudentWelcome || showTeacherWelcome) && index === 0)"
               :class="['message',
                        message.role === 'user' ? 'user-message' :
                        message.role === 'assistant' ? 'ai-message' :
                        message.role === 'function_call' ? 'function-call-message' : 'function-message']">

            <div v-if="!isInlineFormMessage(message)" class="message-header">
              <i v-if="message.role === 'user'" class="fas fa-user"></i>
              <i v-if="message.role === 'assistant'" class="fas fa-robot"></i>
              <i
                v-if="message.role === 'function_call' && message.functionName !== 'reminder_saved' && message.functionName !== 'email_reminder_sent' && message.functionName !== 'user_profile_saved'"
                class="fas fa-code"
              ></i>
              <i v-if="message.role === 'function_call' && message.functionName === 'reminder_saved'" class="fas fa-bell"></i>
              <i v-if="message.role === 'function_call' && message.functionName === 'email_reminder_sent'" class="fas fa-envelope"></i>
              <i v-if="message.role === 'function_call' && message.functionName === 'user_profile_saved'" class="fas fa-id-card"></i>
              <i v-if="message.role === 'function_result'" class="fas fa-cog"></i>
              {{
                message.role === 'user'
                  ? '您'
                  : message.role === 'assistant'
                    ? '智学伴'
                          : message.role === 'function_call'
                      ? message.isClassroomTaskConfirm
                        ? '待确认'
                        : message.isClassroomDocImport
                          ? '导入任务'
                          : message.classroomResult
                            ? '教学任务'
                            : message.functionName === 'reminder_saved'
                          ? '学习提醒'
                          : message.functionName === 'email_reminder_sent'
                            ? '邮件提醒'
                            : message.functionName === 'user_profile_saved'
                              ? '用户信息'
                              : teacherMode
                                ? '教学工具'
                                : '智能体任务'
                      : '执行结果: ' + message.functionName
              }}
            </div>

            <!-- 学习提醒已保存（工具真实写库 + 邮件/定时任务；后端 JSON displayKind=reminder_saved） -->
            <div v-if="message.functionName === 'reminder_saved'" class="cf-result">
              <div class="cf-result-icon">
                <i class="fas fa-check"></i>
              </div>
              <div class="cf-result-body">
                <div class="cf-result-title">提醒已保存</div>
                <div class="cf-result-row">
                  <span class="rk">事项</span>
                  <span class="rv">{{ message.reminderSaved?.title || '—' }}</span>
                </div>
                <div class="cf-result-row">
                  <span class="rk">时间</span>
                  <span class="rv">{{ message.reminderSaved?.eventDateTimeLabel || '—' }}</span>
                </div>
                <div class="cf-result-row">
                  <span class="rk">响铃</span>
                  <span class="rv">{{ message.reminderSaved?.reminderAtLabel || '—' }}</span>
                </div>
                <div class="cf-result-row">
                  <span class="rk">提前</span>
                  <span class="rv">{{ message.reminderSaved?.advanceMinutes ?? '—' }} 分钟</span>
                </div>
                <p v-if="message.reminderSaved?.hint" class="cf-result-hint">{{ message.reminderSaved.hint }}</p>
              </div>
            </div>

            <!-- 学习提醒邮件已发送（与定时提醒成功卡同一套信息层次） -->
            <div v-else-if="message.functionName === 'email_reminder_sent'" class="cf-result">
              <div class="cf-result-icon">
                <i class="fas fa-check"></i>
              </div>
              <div class="cf-result-body">
                <div class="cf-result-title">邮件已发送</div>
                <div class="cf-result-row">
                  <span class="rk">收件人</span>
                  <span class="rv">{{ message.emailReminderSaved?.recipient || '—' }}</span>
                </div>
                <div class="cf-result-row">
                  <span class="rk">主题</span>
                  <span class="rv">{{ message.emailReminderSaved?.subject || '—' }}</span>
                </div>
                <div class="cf-result-row">
                  <span class="rk">摘要</span>
                  <span class="rv">{{ message.emailReminderSaved?.bodyPreview || '—' }}</span>
                </div>
              </div>
            </div>

            <!-- 教师：作业确认卡片 -->
            <div v-else-if="message.isClassroomTaskConfirm" class="classroom-confirm-wrap">
              <ClassroomTaskConfirmCard
                :kind="message.classroomConfirmKind"
                :preview="message.classroomConfirmPreview"
                :summary="message.classroomConfirmSummary"
                @confirmed="(p) => onTaskConfirmed(message, p)"
                @cancel="message.isClassroomTaskConfirm = false"
              />
            </div>

            <!-- 教师：文档/模板导入 -->
            <div v-else-if="message.isClassroomDocImport" class="classroom-doc-wrap">
              <ClassroomDocumentImportCard
                @parsed="(p) => onDocParsed(message, p)"
                @cancel="message.isClassroomDocImport = false"
              />
            </div>

            <!-- 教师端课堂任务执行结果 -->
            <div v-else-if="message.classroomResult" class="classroom-result-wrap">
              <ClassroomResultCard
                :kind="message.classroomResult.kind"
                :summary="message.classroomResult.summary"
                :data="message.classroomResult.data"
                @remind-sent="(r) => onRemindSent(message, r)"
              />
            </div>

            <div v-else-if="message.functionName === 'user_profile_saved'" class="cf-result">
              <div class="cf-result-icon">
                <i class="fas fa-check"></i>
              </div>
              <div class="cf-result-body">
                <div class="cf-result-title">资料已保存</div>
                <div class="cf-result-row">
                  <span class="rk">编号</span>
                  <span class="rv">{{ message.userProfileSaved?.id ?? '—' }}</span>
                </div>
                <div class="cf-result-row">
                  <span class="rk">说明</span>
                  <span class="rv">{{ message.userProfileSaved?.message || '保存成功' }}</span>
                </div>
              </div>
            </div>

            <!-- 复习节奏：先填关键信息（短句触发，不打 LLM） -->
            <div v-else-if="message.isReviewRhythmForm || message.isReviewPlanForm" class="review-rhythm-form-wrap">
              <ReviewRhythmFormCard
                :mode="message.isReviewPlanForm ? 'plan' : 'rhythm'"
                @submit="(data) => (message.isReviewPlanForm ? handleReviewPlanFormSubmit(message, data) : handleReviewRhythmFormSubmit(message, data))"
                @cancel="message.isReviewPlanForm ? handleReviewPlanFormCancel(message) : handleReviewRhythmFormCancel(message)"
              />
            </div>

            <!-- 复习节奏：生成后的精美结果卡 -->
            <div v-else-if="message.isReviewRhythmPlan" class="review-rhythm-plan-wrap">
              <ReviewRhythmPlanCard
                :content="message.content"
                :streaming="!!message.isStreaming"
                :variant="message.reviewPlanVariant === 'plan' ? 'plan' : 'rhythm'"
                :can-retry="!!message.reviewRhythmPrompt && !message.isStreaming && message.reviewRhythmGenerationOk === false"
                @retry="handleReviewRhythmRetry(message)"
              />
            </div>

            <!-- 学习提醒：短句触发，先填卡片（不打 LLM） -->
            <div v-else-if="message.isLearningReminderForm" class="learning-reminder-form-wrap">
              <LearningReminderFormCard
                :prefill="message.reminderFormPrefill || null"
                @submit="(data) => handleLearningReminderFormSubmit(message, data)"
                @cancel="handleLearningReminderFormCancel(message)"
              />
            </div>

            <!-- 邮件提醒：短句或 LLM send_email，与学习提醒同一套卡片流程 -->
            <div v-else-if="message.isEmailReminderForm" class="learning-email-reminder-form-wrap">
              <LearningEmailReminderFormCard
                :prefill="message.emailFormPrefill || null"
                @submit="(data) => handleEmailReminderFormSubmit(message, data)"
                @cancel="handleEmailReminderFormCancel(message)"
              />
            </div>

            <div v-else-if="message.isUserProfileForm" class="user-profile-form-wrap">
              <UserProfileFormCard
                :prefill="message.userProfileFormPrefill || null"
                @submit="(data) => handleUserProfileFormSubmit(message, data)"
                @cancel="handleUserProfileFormCancel(message)"
              />
            </div>

            <!-- 应用内页面一键跳转（个人中心、消息、作业等） -->
            <div v-else-if="message.isAppNavJump" class="app-nav-jump-wrap">
              <p v-if="message.content" class="app-nav-jump-lead">{{ message.content }}</p>
              <AppNavJumpCard :target="message.appNavTarget" />
            </div>

            <!-- 消息查询与汇总（真实拉取 /api/notifications） -->
            <div v-else-if="message.isMessagesSummary" class="messages-summary-wrap">
              <MessagesSummaryCard
                :loading="!!message.messagesSummaryLoading"
                :error="message.messagesSummaryError || ''"
                :summary="message.messagesSummaryData?.summary || ''"
                :unread="message.messagesSummaryData?.unread ?? 0"
                :unread-items="message.messagesSummaryData?.unreadItems || []"
                :empty="!!message.messagesSummaryData?.empty"
                @retry="() => retryMessagesSummary(message)"
              />
            </div>

            <!-- 作业待办查询（真实拉取 /api/classroom/my-assignments/summary） -->
            <div v-else-if="message.isAssignmentsSummary" class="assignments-summary-wrap">
              <AssignmentsSummaryCard
                :loading="!!message.assignmentsSummaryLoading"
                :error="message.assignmentsSummaryError || ''"
                :summary="message.assignmentsSummaryData?.summary || ''"
                :pending-count="message.assignmentsSummaryData?.pendingCount ?? 0"
                :total-tasks="message.assignmentsSummaryData?.totalTasks ?? 0"
                :pending-items="message.assignmentsSummaryData?.pendingItems || []"
                :empty="!!message.assignmentsSummaryData?.empty"
                @retry="() => retryAssignmentsSummary(message)"
              />
            </div>

            <!-- 教师：班级学情洞察（待交 / 表现好 / 学情概况，真实数据 + AI 总结） -->
            <div v-else-if="message.isTeacherClassInsightSummary" class="teacher-class-insight-summary-wrap">
              <TeacherClassInsightSummaryCard
                :loading="!!message.teacherClassInsightSummaryLoading"
                :error="message.teacherClassInsightSummaryError || ''"
                :summary="message.teacherClassInsightSummaryData?.summary || ''"
                :focus="message.teacherClassInsightSummaryData?.focus || 'general'"
                :focus-label="message.teacherClassInsightSummaryData?.focusLabel || '班级学情'"
                :class-completion-percent="message.teacherClassInsightSummaryData?.classCompletionPercent"
                :highlight-students="message.teacherClassInsightSummaryData?.highlightStudents || []"
                :latest-task-id="message.teacherClassInsightSummaryData?.latestTaskId || ''"
                :empty="!!message.teacherClassInsightSummaryData?.empty"
                @retry="() => retryTeacherClassInsightSummary(message)"
              />
            </div>

            <!-- 教师：学生提交概况（真实拉取 /api/classroom/submissions/recent-summary） -->
            <div v-else-if="message.isTeacherSubmissionsSummary" class="teacher-submissions-summary-wrap">
              <TeacherSubmissionsSummaryCard
                :loading="!!message.teacherSubmissionsSummaryLoading"
                :error="message.teacherSubmissionsSummaryError || ''"
                :summary="message.teacherSubmissionsSummaryData?.summary || ''"
                :pending-count="message.teacherSubmissionsSummaryData?.pendingCount ?? 0"
                :total-submissions="message.teacherSubmissionsSummaryData?.totalSubmissions ?? 0"
                :pending-items="message.teacherSubmissionsSummaryData?.pendingItems || []"
                :empty="!!message.teacherSubmissionsSummaryData?.empty"
                @retry="() => retryTeacherSubmissionsSummary(message)"
              />
            </div>

            <!-- 学习日程提醒表单（后端函数 create_travel_reminder / setTravelReminder 弹窗编辑） -->
            <div v-else-if="message.functionName === 'setTravelReminder' || message.functionName === 'create_travel_reminder'" class="travel-reminder-container">
              <TravelReminder
                  :initial-data="message.reminderData || {}"
                  @cancel="cancelTravelReminder(message)"
                  @save="saveTravelReminder(message, $event)"
              />
            </div>

            <!-- 天气 / 出行锦囊（无地图、无行程） -->
            <div v-else-if="message.isWeatherResult" class="weather-result-wrap">
              <TravelBriefCard
                :title="message.weatherCardTitle || ''"
                :hint="message.weatherCardHint || '天气 · 穿衣 · 小贴士（精简）'"
                :raw-weather="message.weatherRaw || ''"
                :content="message.content"
              />
            </div>

            <!-- 路线地图 -->
            <div v-else-if="message.functionName === 'getLocationInfo' && message.mapVisible" class="assistant-message">
              <strong>路线地图</strong>
              <div class="route-map-container">
                <WalkingRoute
                    :origin="message.origin"
                    :destination="message.destination"
                    style="width: 600px; height: 700px;"
                />
              </div>
            </div>

            <!-- 美食推荐（腾讯地图 / MCPServer food_search） -->
            <div v-else-if="message.functionName === 'food_search'" class="food-result-wrapper">
              <FoodResult :data="message.foodData" />
            </div>

            <!-- 酒店推荐结果 -->
            <div v-else-if="message.functionName === 'hotel_recommend'" class="hotel-result-wrapper">
              <HotelResult :result="message.hotelData" @book="handleHotelBook" />
            </div>

            <!-- 酒店预订表单 -->
            <div v-else-if="message.showBookingForm && message.bookingHotel" class="hotel-booking-wrapper">
              <HotelBooking
                :hotel="message.bookingHotel"
                :initial-data="message.bookingPrefill || {}"
                @cancel="cancelHotelBooking(message)"
                @book="submitHotelBooking"
              />
            </div>

            <!-- 酒店预订结果 -->
            <div v-else-if="message.functionName === 'hotel_book'" class="hotel-book-result">
              <div class="book-success">
                <i class="fas fa-check-circle"></i>
                <div class="book-info">
                  <div class="book-title">✅ 预订请求已记录</div>
                  <div>订单号：{{ message.hotelBookData?.orderId || '—' }}</div>
                  <div>酒店：{{ message.hotelBookData?.hotelName || '—' }}</div>
                  <div>入住：{{ message.hotelBookData?.checkInDate }} → {{ message.hotelBookData?.checkOutDate }}
                    （{{ message.hotelBookData?.nights || 0 }}晚）</div>
                  <div>联系人：{{ message.hotelBookData?.contactName }} · {{ message.hotelBookData?.contactPhone }}</div>
                  <div class="book-link" v-if="message.hotelBookData?.bookingLink">
                    <a :href="message.hotelBookData.bookingLink" target="_blank">
                      <i class="fas fa-external-link-alt"></i> 前往 TripAdvisor 完成支付预订
                    </a>
                  </div>
                </div>
              </div>
            </div>

            <!-- 行程规划地图 -->
            <div v-else-if="message.functionName === 'plan_itinerary'" class="assistant-message">
              <TripPlan :plan-data="message.planData" />
            </div>

            <!-- 智学伴普通回复：轻量渲染标题/列表/加粗；用户资料类意图可点底部按钮打开填写卡片 -->
            <div
              v-else-if="
                message.role === 'assistant' &&
                !message.isReviewRhythmPlan &&
                !message.isWeatherResult &&
                !message.isUserProfileForm &&
                !message.isEmailReminderForm &&
                !message.isLearningReminderForm
              "
              class="assistant-plain-bubble"
            >
              <div class="ai-text-prose ai-study-rich" v-html="renderAssistantStudyHtml(message.content)" />
              <div
                v-if="message.showUserProfileInsertCta && !message.isStreaming && !message.profileInsertFormOpened"
                class="profile-insert-cta-bar"
              >
                <button type="button" class="profile-insert-cta-btn" @click="openUserProfileFormFromAssistant(message)">
                  <i class="fas fa-id-card"></i>
                  填写并保存资料
                </button>
                <span class="profile-insert-cta-hint">核对信息后即可保存</span>
              </div>
            </div>
            <!-- 用户与其它角色：纯文本 -->
            <div v-else class="ai-text-prose chat-plain-text">
              {{ message.content }}
            </div>
          </div>

          <!-- 加载指示器 -->
          <div v-if="isLoading" class="typing-indicator">
            <span>智学伴正在思考…</span>
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
          </div>

        </div>

        <!-- 输入区域 -->
        <div v-if="!isTeacherShell && !teacherMode" class="s-chat-compose">
          <div
            v-if="showStudentWelcome"
            class="s-mobile-chips"
            aria-label="快捷指令"
          >
            <button
              v-for="(tpl, i) in promptTemplates"
              :key="'m-' + i"
              type="button"
              class="s-mobile-chip"
              @click="applyTemplate(tpl)"
            >
              <i :class="templateIcon(tpl)"></i>
              <span>{{ tpl.label }}</span>
            </button>
          </div>

          <div class="chat-input chat-input--floating">
            <div class="chat-input-bar">
              <textarea
                  v-model="userInput"
                  :placeholder="studentInputPlaceholder"
                  @keyup.enter.exact="sendMessage"
                  :disabled="isLoading"
                  rows="1"
              ></textarea>
              <button
                  v-if="isLoading"
                  type="button"
                  class="stop-button"
                  @click="stopGeneration"
                  aria-label="停止"
              >
                <i class="fas fa-stop"></i>
                <span class="send-btn-label">停止</span>
              </button>
              <button
                  v-else
                  class="send-button"
                  @click="sendMessage"
                  :disabled="userInput.trim() === ''"
                  aria-label="发送"
              >
                <i class="fas fa-paper-plane"></i>
                <span class="send-btn-label">发送</span>
              </button>
            </div>
          </div>
        </div>

        <template v-else-if="isTeacherShell">
          <div
            v-if="!showTeacherWelcome"
            class="s-mobile-chips s-mobile-chips--teacher"
            aria-label="快捷指令"
          >
            <button
              v-for="(tpl, i) in promptTemplates"
              :key="'tm-' + i"
              type="button"
              class="s-mobile-chip"
              @click="applyTemplate(tpl)"
            >
              <i :class="templateIcon(tpl)"></i>
              <span>{{ tpl.label }}</span>
            </button>
          </div>
          <div class="chat-input">
            <div class="chat-input-bar">
              <textarea
                  v-model="userInput"
                  :placeholder="inputPlaceholder"
                  @keyup.enter.exact="sendMessage"
                  :disabled="isLoading"
              ></textarea>
              <button
                  v-if="isLoading"
                  type="button"
                  class="stop-button"
                  @click="stopGeneration"
              >
                <i class="fas fa-stop"></i>
                <span class="send-btn-label">停止</span>
              </button>
              <button
                  v-else
                  class="send-button"
                  @click="sendMessage"
                  :disabled="userInput.trim() === ''"
                  aria-label="发送"
              >
                <i class="fas fa-paper-plane"></i>
                <span class="send-btn-label">发送</span>
              </button>
            </div>
          </div>
        </template>

        <div v-else-if="teacherMode" class="chat-input">
          <div class="chat-input-bar">
            <textarea
                v-model="userInput"
                :placeholder="inputPlaceholder"
                @keyup.enter.exact="sendMessage"
                :disabled="isLoading"
            ></textarea>
            <button
                v-if="isLoading"
                type="button"
                class="stop-button"
                @click="stopGeneration"
            >
              <i class="fas fa-stop"></i>
              <span class="send-btn-label">停止</span>
            </button>
            <button
                v-else
                class="send-button"
                @click="sendMessage"
                :disabled="userInput.trim() === ''"
                aria-label="发送"
            >
              <i class="fas fa-paper-plane"></i>
              <span class="send-btn-label">发送</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <StudentBottomNav
    v-if="!teacherMode && !embedded"
    active="chat"
    :unread-count="studentUnread"
  />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import StudentBottomNav from '@/components/student/StudentBottomNav.vue';

const props = defineProps({
  teacherMode: { type: Boolean, default: false },
  embedded: { type: Boolean, default: false },
});

const isTeacherShell = computed(() => props.embedded && props.teacherMode);
import apiService from '@/services/api';
import authApi from '@/services/authApi';
import { getToken, clearAuth } from '@/stores/auth';
import WalkingRoute from '@/components/WalkingRoute.vue';
import TravelReminder from '@/components/TravelReminder.vue';
import HotelResult from '@/components/HotelResult.vue';
import HotelBooking from '@/components/HotelBooking.vue';
import FoodResult from '@/components/FoodResult.vue';
import TripPlan from '@/components/TripPlan.vue';
import TravelBriefCard from '@/components/TravelBriefCard.vue';
import ReviewRhythmFormCard from '@/components/ReviewRhythmFormCard.vue';
import ReviewRhythmPlanCard from '@/components/ReviewRhythmPlanCard.vue';
import LearningReminderFormCard from '@/components/LearningReminderFormCard.vue';
import LearningEmailReminderFormCard from '@/components/LearningEmailReminderFormCard.vue';
import UserProfileFormCard from '@/components/UserProfileFormCard.vue';
import ClassroomResultCard from '@/components/ClassroomResultCard.vue';
import ClassroomTaskConfirmCard from '@/components/ClassroomTaskConfirmCard.vue';
import ClassroomDocumentImportCard from '@/components/ClassroomDocumentImportCard.vue';
import AppNavJumpCard from '@/components/AppNavJumpCard.vue';
import MessagesSummaryCard from '@/components/MessagesSummaryCard.vue';
import AssignmentsSummaryCard from '@/components/AssignmentsSummaryCard.vue';
import TeacherSubmissionsSummaryCard from '@/components/TeacherSubmissionsSummaryCard.vue';
import TeacherClassInsightSummaryCard from '@/components/TeacherClassInsightSummaryCard.vue';
import { detectAppNavIntent } from '@/constants/appNavIntents';
import { wantsMessagesSummary } from '@/utils/messageSummary';
import { wantsAssignmentSummary } from '@/utils/assignmentSummary';
import { wantsTeacherSubmissionsSummary } from '@/utils/teacherSubmissionSummary';
import { detectTeacherClassInsight } from '@/utils/teacherClassInsightSummary';
import classroomApi from '@/services/classroomApi';
import { loadChatSession, saveChatSession } from '@/composables/chatSessionStore';
import notificationsApi from '@/services/notificationsApi';
import { fetchEventSource } from '@microsoft/fetch-event-source';

const router = useRouter();
const studentUnread = ref(0);

async function refreshStudentUnread() {
  if (props.teacherMode) return;
  try {
    const res = await notificationsApi.unreadCount();
    studentUnread.value = res?.count ?? 0;
  } catch {
    studentUnread.value = 0;
  }
}

async function handleLogout() {
  try {
    await authApi.logout();
  } finally {
    clearAuth();
    router.push('/login');
  }
}

const API_BASE = import.meta.env.VITE_API_BASE || '';
const CHAT_SSE_PATH = '/api/prompt/stream';

const studentPromptTemplates = [
  { label: '整理知识点', text: '请帮我整理 JavaSE 核心知识点，适合期末复习。' },
  { label: '智能答疑', text: 'Java 里的多线程有哪些入门要点？请用通俗说法解释。' },
  { label: '复习计划', text: '制定一周数据结构复习计划，工作日每天约 2 小时。' },
  { label: '邮件提醒', text: '发邮件到我的邮箱（请替换为真实邮箱），主题是今晚复习提醒，内容是 20:00 刷算法题。' },
  { label: '定时提醒', text: '每天晚上 20:00 提醒我背英语单词，提前 15 分钟通知。' },
  { label: '保存我的信息', text: '我想把常用信息存进用户资料表：邮箱 demo@example.com，手机 13800138000。' },
];

const teacherPromptTemplates = [
  {
    label: '发布作业',
    text: '请创建并发布「自主决策 Agent」课堂作业，含 3 个子任务：需求分析、方案设计、成果交付。',
  },
  { label: '从文档建任务', action: 'document_import' },
  { label: '催交提醒', text: '请对最新已发布作业直接向未交学生发送催交邮件。' },
  { label: '批量批改', text: '请对最新已发布作业中待批改的提交批量生成 AI 批改建议。' },
  { label: '学情看板', text: '请生成最新已发布作业的班级学情进度看板。' },
];

const promptTemplates = computed(() =>
  props.teacherMode ? teacherPromptTemplates : studentPromptTemplates,
);

const chatHeaderTitle = computed(() =>
  props.teacherMode ? '教学对话 · 一句话布置与跟进' : '对话 · 一句话说出你的学习需求',
);

const inputPlaceholder = computed(() =>
  props.teacherMode
    ? '描述教学需求，例如：设计课堂作业、批改建议、学情分析、出题辅助…'
    : '用一句话描述你的学习需求，例如：整理 JavaSE 知识点、定时提醒、答疑或复习计划…',
);

const studentInputPlaceholder = '说说你的学习需求…';

const welcomeShortText = '整理笔记、智能答疑、复习计划 — 点下方快捷词或直接输入';

const welcomeIntroText =
  '就像坐在你旁边一起自习的老朋友——整理笔记、讲难题、排复习节奏，随口说一句就行。';

const welcomeCapabilities = [
  {
    label: '整理知识点',
    hint: '笔记结构化 · 期末冲刺',
    icon: 'fas fa-book-open',
    tpl: studentPromptTemplates[0],
  },
  {
    label: '智能答疑',
    hint: '难点讲明白 · 一听就懂',
    icon: 'fas fa-lightbulb',
    tpl: studentPromptTemplates[1],
  },
  {
    label: '复习计划',
    hint: '定制周期 · 每天学什么',
    icon: 'fas fa-calendar-check',
    tpl: studentPromptTemplates[2],
  },
  {
    label: '学习提醒',
    hint: '邮件 / 定时 · 到点不忘',
    icon: 'fas fa-bell',
    tpl: studentPromptTemplates[3],
  },
];

const teacherWelcomeIntro =
  '创建/发布作业前会先让您确认；催交、批量批改、学情看板均可一句话完成。';

const teacherWelcomeCapabilities = [
  {
    label: '发布作业',
    hint: '创建并发布课堂任务',
    icon: 'fas fa-paper-plane',
    tpl: teacherPromptTemplates[0],
  },
  {
    label: '从文档建任务',
    hint: '导入任务书快速建课',
    icon: 'fas fa-file-import',
    tpl: teacherPromptTemplates[1],
  },
  {
    label: '催交提醒',
    hint: '一键提醒未交学生',
    icon: 'fas fa-bell',
    tpl: teacherPromptTemplates[2],
  },
  {
    label: '批量批改',
    hint: 'AI 建议 · 您来确认',
    icon: 'fas fa-clipboard-check',
    tpl: teacherPromptTemplates[3],
  },
];

const welcomeText = props.teacherMode
  ? '您好，我是教学 Agent～创建/发布作业前会先让您确认；也可用「从文档建任务」导入任务书。催交、批量批改、学情看板均可一句话执行。'
  : '嗨，我是智学伴～就像坐在你旁边一起自习的老朋友。不管是整理笔记、帮你把难题讲明白，还是咱们一起排个复习节奏、设个提醒，你随口说一句就行。要是你还想查查天气、出门转转，我也会陪着你慢慢搞定。';

function buildInitialMessages() {
  const saved = loadChatSession(props.teacherMode);
  if (saved?.length) return saved;
  return [{ role: 'assistant', content: welcomeText }];
}

const userInput = ref('');
const messages = ref(buildInitialMessages());

const applyTemplate = (tpl) => {
  if (isLoading.value) return;
  if (tpl.action === 'document_import') {
    openDocumentImportCard();
    return;
  }
  if (tpl.text) {
    userInput.value = tpl.text;
  }
};

function templateIcon(tpl) {
  if (tpl.action === 'document_import') return 'fas fa-file-import';
  const map = {
    发布作业: 'fas fa-paper-plane',
    催交提醒: 'fas fa-bell',
    批量批改: 'fas fa-clipboard-check',
    学情看板: 'fas fa-chart-line',
    整理知识点: 'fas fa-book-open',
    智能答疑: 'fas fa-lightbulb',
    复习计划: 'fas fa-calendar-check',
    邮件提醒: 'fas fa-envelope',
    定时提醒: 'fas fa-clock',
    保存我的信息: 'fas fa-id-card',
  };
  return map[tpl.label] || 'fas fa-magic';
}

function openDocumentImportCard() {
  dismissStaleInlineForms();
  messages.value.push({
    role: 'function_call',
    functionName: 'document_import',
    isClassroomDocImport: true,
  });
  scrollToBottom();
}

function showTaskPreviewCard(kind, preview, summary) {
  dismissStaleInlineForms();
  messages.value.push({
    role: 'function_call',
    functionName: kind === 'classroom_publish_preview' ? 'publish_preview' : 'task_preview',
    isClassroomTaskConfirm: true,
    classroomConfirmKind: kind,
    classroomConfirmPreview: preview,
    classroomConfirmSummary: summary || '',
  });
  scrollToBottom();
}

function onTaskConfirmed(message, { kind, data }) {
  message.isClassroomTaskConfirm = false;
  messages.value.push({
    role: 'function_call',
    functionName: kind,
    classroomResult: {
      kind,
      summary: kind === 'classroom_task_published' ? '学生端已可见' : '',
      data,
    },
  });
  scrollToBottom();
}

function onDocParsed(message, preview) {
  message.isClassroomDocImport = false;
  showTaskPreviewCard('classroom_task_preview', preview, '文档已解析，请确认后创建');
}

function onRemindSent(message, result) {
  message.classroomResult = {
    kind: 'classroom_remind_sent',
    summary: `已向 ${result.inAppCount ?? 0} 人发送站内消息，邮件 ${result.sentCount ?? 0} 封`,
    data: result,
  };
  scrollToBottom();
}

const isLoading = ref(false);
const messagesContainer = ref(null);

const TEACHER_MOBILE_MQ = '(max-width: 960px)';
const isTeacherMobileViewport = ref(false);
let teacherMobileMediaQuery = null;

function syncTeacherMobileViewport() {
  if (typeof window === 'undefined') return;
  isTeacherMobileViewport.value = window.matchMedia(TEACHER_MOBILE_MQ).matches;
}

const showStudentWelcome = computed(
  () =>
    !props.teacherMode &&
    !isLoading.value &&
    messages.value.length === 1 &&
    messages.value[0]?.role === 'assistant',
);

const showTeacherWelcome = computed(
  () =>
    isTeacherShell.value &&
    isTeacherMobileViewport.value &&
    !isLoading.value &&
    messages.value.length === 1 &&
    messages.value[0]?.role === 'assistant',
);
/** 递增后用于忽略已被新请求取代的旧 SSE 流的 onerror */
let activeStreamId = 0;
/** 当前 SSE 请求的 AbortController，用于停止生成 */
let activeAbortController = null;

const stopGeneration = () => {
  if (!isLoading.value) return;
  activeStreamId += 1;
  if (activeAbortController) {
    activeAbortController.abort();
    activeAbortController = null;
  }
  for (const m of messages.value) {
    if (!m.isStreaming) continue;
    m.isStreaming = false;
    const text = typeof m.content === 'string' ? m.content.trim() : '';
    if (!text) {
      m.content = '已停止生成。';
    } else if (!text.endsWith('（已停止生成）')) {
      m.content += '\n\n（已停止生成）';
    }
  }
  isLoading.value = false;
  scrollToBottom();
};

/** 用户只说「排复习节奏」等短句时，先弹出信息卡片 */
function wantsReviewRhythmCard(text) {
  const t = text.trim();
  if (t.length > 56) return false;
  const c = t.replace(/\s/g, '');
  const needles = [
    '排个复习节奏',
    '排复习节奏',
    '安排复习节奏',
    '帮我排复习节奏',
    '一起排个复习节奏',
    '一起排复习节奏',
    '定个复习节奏',
    '来个复习节奏',
    '复习节奏安排',
    '帮我安排复习节奏',
    '制定复习节奏',
    '规划复习节奏',
    '排一下复习节奏',
    '安排一下复习节奏'
  ];
  if (needles.some((n) => c.includes(n.replace(/\s/g, '')))) return true;
  if (/^排.{0,4}复习节奏$/.test(c)) return true;
  if (c === '复习节奏' || c === '排复习节奏') return true;
  return false;
}

/** 「制定一周数据结构复习计划…」等与后端学业意图一致时，用复习节奏同款结果卡展示 */
function wantsReviewPlanCard(text) {
  const t = text.trim();
  if (t.length < 4 || t.length > 1200) return false;
  if (/^(?:不|别|不要|无需|算了)/.test(t)) return false;
  if (/复习计划|学习计划|备考计划/.test(t)) return true;
  if (/(?:制定|定制|帮忙|帮我|请)(?:个|一)?.{0,40}(?:复习|备考).{0,20}计划/.test(t)) return true;
  if (/.{0,24}计划.{0,40}(?:复习|备考|期末|考试)/.test(t) && t.length <= 420) return true;
  return false;
}

/** 已包含周期、每日投入、科目方向等，可直接请求模型生成 */
function hasReviewPlanRichContext(text) {
  const t = text.trim();
  if (t.length >= 46) return true;
  if (
    /\d+\s*(天|周|日)|[一二三四五六七八九十两]+\s*天|[一二三四五六七八九十两]+\s*周|半\s*个月|一个\s*月|工作日|周末|每天|每日|每周|每个|小时|分钟/.test(
      t
    )
  ) {
    return true;
  }
  if (
    /数据结构|操作系统|计算机网络|离散|线代|高数|微积分|概率|英语|政治|马原|毛概|近代史|思修|四级|六级|考研|Java|Python|C\+\+|算法|计组|数据库|软件工程|编译|电路|模电|力学|有机|无机|生化|细胞|西综|法考|教资|雅思|托福|GRE|期末|期中|复试|笔试|面试|科目|课程|单元|章节|单词|语法|阅读|听力/.test(
      t
    )
  ) {
    return true;
  }
  return false;
}

/** 用户想保存个人资料到 user_profile / 数据库，但模型可能只回了说明文字而未输出工具 JSON */
function wantsUserProfileInsertHint(text) {
  const t = (text || '').trim();
  if (t.length < 4 || t.length > 2000) return false;
  if (/^(?:不|别|不要|算了)/.test(t)) return false;
  if (
    /保存用户信息|保存.{0,12}用户信息|用户资料表|用户资料.{0,12}(?:存|保存|写入)|存.{0,10}用户资料|user_profile|常用信息.{0,8}存|存进.{0,12}用户|写入.{0,12}用户|录入.{0,10}联系|插入.{0,8}用户/i.test(
      t
    )
  ) {
    return true;
  }
  if (/(?:存|保存|写入|录入).{0,20}用户信息|用户信息.{0,16}(?:存|保存|写入|录入|表)/i.test(t)) return true;
  if (/存.*数据库.*(邮箱|手机|电话|姓名)|把.*存到.*(用户|资料|表)|帮我存.*(邮箱|手机)/i.test(t)) return true;
  return false;
}

function tryExtractProfilePrefillFromPlainText(raw) {
  const t = String(raw || '').replace(/\r\n/g, '\n');
  const prefill = {};
  const emailM = t.match(/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/);
  if (emailM) prefill.email = emailM[0];
  const phones = t.match(/1[3-9]\d{9}/g);
  if (phones && phones.length) prefill.phone = phones[phones.length - 1];
  return prefill;
}

function finalizeProfileInsertCta(idx) {
  const m = messages.value[idx];
  if (!m || m.role !== 'assistant' || !m.profileInsertCtaEligible) return;
  if (m.isReviewRhythmPlan || m.isWeatherResult) return;
  const c = typeof m.content === 'string' ? m.content.trim() : '';
  if (c.length < 8) return;
  if (/^\s*\{[\s\S]*"function"\s*:/.test(c)) return;
  m.showUserProfileInsertCta = true;
}

function openUserProfileFormFromAssistant(assistantMsg) {
  const idx = messages.value.indexOf(assistantMsg);
  if (idx < 0) return;
  assistantMsg.profileInsertFormOpened = true;
  const prefill = tryExtractProfilePrefillFromPlainText(assistantMsg.content || '');
  messages.value.splice(idx + 1, 0, {
    role: 'assistant',
    content: '',
    isUserProfileForm: true,
    userProfileFormPrefill: Object.keys(prefill).length ? prefill : null
  });
  scrollToBottom();
}

const REMINDER_ADVANCE_OPTIONS = ['0', '5', '15', '30', '60', '1440'];

function nearestAdvanceOption(minutes) {
  const m = Number(minutes);
  if (!Number.isFinite(m) || m < 0) return null;
  const nums = REMINDER_ADVANCE_OPTIONS.map(Number);
  let best = nums[0];
  let bestDiff = Math.abs(m - best);
  for (const n of nums) {
    const d = Math.abs(m - n);
    if (d < bestDiff) {
      best = n;
      bestDiff = d;
    }
  }
  return String(best);
}

function pad2(n) {
  return String(n).padStart(2, '0');
}

function toDatetimeLocalFromDate(d) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
}

/**
 * 从用户自然语言里尽量抽出标题、时间、提前量等；抽不到的字段为 null，留给用户手填。
 */
function parseReminderPrefill(raw) {
  const text = (raw || '').trim();
  const out = {
    title: null,
    datetimeLocal: null,
    advanceNotice: null,
    location: null,
    notes: null,
    email: null,
    repeatDaily: false
  };
  if (!text) return out;
  if (/不要提醒|别提醒|取消提醒/.test(text)) return out;

  let m = text.match(/提前\s*(\d+)\s*分钟/);
  if (m) out.advanceNotice = nearestAdvanceOption(parseInt(m[1], 10));
  else {
    m = text.match(/提前\s*(\d+)\s*个?\s*小时/);
    if (m) out.advanceNotice = nearestAdvanceOption(parseInt(m[1], 10) * 60);
    else if (/提前\s*半\s*个?\s*小时|提前半小时/.test(text)) out.advanceNotice = '30';
  }

  m = text.match(/提醒我\s*(?:要|一下|记得)?\s*(.+?)(?=，|。|$|提前|通知)/);
  if (!m) m = text.match(/记得\s*提醒我\s*(.+?)(?=，|。|$|提前|通知)/);
  if (!m) m = text.match(/叫我\s*(.+?)(?=，|。|$|提前|通知)/);
  if (m) {
    let t0 = m[1].trim().replace(/^[:：]\s*/, '');
    if (t0.length > 0 && t0.length <= 80) out.title = t0;
  }

  let hour = null;
  let minute = 0;
  m = text.match(/(?:^|[^\d])(\d{1,2}):(\d{2})(?:[^\d]|$)/);
  if (m) {
    hour = parseInt(m[1], 10);
    minute = parseInt(m[2], 10);
  }
  if (hour == null) {
    m = text.match(/晚上\s*(\d{1,2})\s*(?:点(?!\d)|[:：]\s*(\d{2}))?/);
    if (m) {
      let h = parseInt(m[1], 10);
      hour = h <= 11 ? h + 12 : h;
      minute = m[2] ? parseInt(m[2], 10) : 0;
    }
  }
  if (hour == null) {
    m = text.match(/(?:早上|上午|凌晨)\s*(\d{1,2})\s*(?:点(?!\d)|[:：]\s*(\d{2}))?/);
    if (m) {
      hour = parseInt(m[1], 10);
      minute = m[2] ? parseInt(m[2], 10) : 0;
      if (/凌晨/.test(text) && hour === 12) hour = 0;
    }
  }
  if (hour == null) {
    m = text.match(/(\d{1,2})\s*点\s*(\d{1,2})\s*分/);
    if (m) {
      hour = parseInt(m[1], 10);
      minute = parseInt(m[2], 10);
    }
  }
  if (hour == null) {
    m = text.match(/(\d{1,2})\s*点(?!半)/);
    if (m) {
      hour = parseInt(m[1], 10);
      minute = 0;
      if (/晚上|傍晚|今夜|今晚/.test(text) && hour < 12) hour += 12;
    }
  }

  const base = new Date();
  base.setSeconds(0, 0);
  if (/明天/.test(text)) base.setDate(base.getDate() + 1);
  else if (/后天/.test(text)) base.setDate(base.getDate() + 2);
  else if (/大后天/.test(text)) base.setDate(base.getDate() + 3);

  if (hour != null && hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
    base.setHours(hour, minute, 0, 0);
    if (!/明天|后天|大后天/.test(text)) {
      const now = new Date();
      if (base.getTime() <= now.getTime()) base.setDate(base.getDate() + 1);
    }
    out.datetimeLocal = toDatetimeLocalFromDate(base);
  }

  if (/每天|每晚|每天晚上|每个晚上/.test(text)) {
    out.repeatDaily = true;
  }

  m = text.match(/在\s*([^，。\s]{2,18})\s*(?:学习|看书|复习|背单词|自习)/);
  if (m && !/提醒/.test(m[1])) out.location = m[1].trim();

  return out;
}

/** 用户说「设个提醒」或带提醒语义的句子时，先弹出填写卡片（不走对话模型） */
function wantsLearningReminderCard(text) {
  const t = text.trim();
  if (t.length > 200) return false;
  if (/发邮件|邮箱|@|smtp/i.test(t)) return false;
  if (/不要提醒|别提醒|取消提醒/.test(t)) return false;
  const c = t.replace(/\s/g, '');
  const needles = [
    '设个提醒',
    '设置提醒',
    '设一下提醒',
    '帮我设提醒',
    '帮我设置提醒',
    '提醒我',
    '提醒我一下',
    '记个提醒',
    '加个提醒',
    '添加提醒',
    '定时提醒',
    '弄个提醒',
    '来个提醒',
    '我要设提醒'
  ];
  if (needles.some((n) => c.includes(n.replace(/\s/g, '')))) return true;
  if (/^设.{0,4}提醒$/.test(c)) return true;
  if (/(提醒|闹钟|定时).*(点|[:：]\d|\d{1,2}:\d{2}|分钟)/.test(c) && c.length <= 80) return true;
  if (/\d{1,2}:\d{2}/.test(c) && /提醒我/.test(c)) return true;
  return false;
}

/** 与后端 SendEmailIntentOverride 对齐：从一句话里抠收件人、主题、正文 */
function parseEmailReminderPrefill(text) {
  const out = {};
  if (!text || typeof text !== 'string') return out;
  let m = text.match(
    /(?:发邮件|发送邮件|写邮件|发一封邮件|发封邮件|邮件发给)\s*(?:给|到|至)?\s*([\w.+-]+@[\w.-]+\.[a-zA-Z0-9]+)/i
  );
  if (m) out.recipient = m[1].trim();
  if (!out.recipient) {
    m = text.match(/(?:邮箱|发到|发到邮箱|收件人)[:：]?\s*([\w.+-]+@[\w.-]+\.[a-zA-Z0-9]+)/i);
    if (m) out.recipient = m[1].trim();
  }
  m = text.match(/主题(?:是|为|[:：])?\s*([^，,；;。\n]+)/);
  if (m) {
    const s = m[1].trim();
    if (s) out.subject = s;
  }
  m = text.match(/内容(?:是|为|[:：])?\s*(.+)$/s);
  if (m) {
    const s = m[1].trim();
    if (s) out.content = s;
  }
  return out;
}

/**
 * 从「给张三发邮件」「发邮箱给张三」等句子里抽出联系人姓名（再去 user_profile 查邮箱）。
 */
function extractRecipientNameForEmail(text) {
  const t = (text || '').trim();
  if (!t) return '';
  let m = t.match(
    /(?:发(?:邮件|电子邮箱?|邮箱)|写邮件|发送邮件).*?给\s*([^\s，,。！？!?；;、]{1,40})/
  );
  if (m) return m[1].trim();
  m = t.match(/给\s*([^\s，,。！？!?；;、]{1,40})\s*发(?:邮件|电子邮箱?|邮箱)/);
  if (m) return m[1].trim();
  m = t.match(/向\s*([^\s，,。！？!?；;、]{1,40})\s*发(?:邮件|电子邮箱?|邮箱)/);
  if (m) return m[1].trim();
  return '';
}

/** 用户明确要发学习相关邮件时，先弹出填写卡片（与定时提醒同一套交互） */
function wantsEmailReminderCard(text) {
  const t = text.trim();
  if (t.length > 220) return false;
  if (/不要发邮件|取消邮件|别发邮件/.test(t)) return false;
  if (wantsUserProfileInsertHint(t)) return false;
  const c = t.replace(/\s/g, '');
  const needles = [
    '发邮件',
    '发送邮件',
    '写邮件',
    '发一封邮件',
    '发封邮件',
    '邮箱提醒',
    '邮件提醒',
    '邮件发给',
    '发一封',
    '发封',
    '发邮箱',
    '发电子邮箱',
    '电子邮箱'
  ];
  if (needles.some((n) => c.includes(n.replace(/\s/g, '')))) return true;
  if (/给.+发(?:邮件|邮箱|电子邮箱)/.test(c)) return true;
  if (/发(?:邮件|邮箱|电子邮箱).*给/.test(c)) return true;
  if (/向.+发(?:邮件|邮箱|电子邮箱)/.test(c)) return true;
  if (
    /@[\w.-]+\.\w{2,}/.test(t) &&
    /(?:邮件|邮箱|发)/.test(t) &&
    t.length <= 180 &&
    !/(?:存|保存|写入|录入).{0,28}(?:数据库|用户|资料)|数据库.{0,24}(?:表|字段)|user_profile/i.test(t)
  ) {
    return true;
  }
  return false;
}

function splitDatetimeLocalForApi(dt) {
  if (!dt || typeof dt !== 'string') return { date: '', time: '' };
  const [d, t] = dt.split('T');
  const timePart = t && t.length >= 5 ? t.slice(0, 5) : '09:00';
  return { date: d || '', time: timePart };
}

function formatReminderZhLabel(d) {
  if (!d || Number.isNaN(d.getTime())) return '—';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}年${m}月${day}日 ${h}:${min}`;
}

function buildTravelReminderMcpParams(data) {
  const { date, time } = splitDatetimeLocalForApi(data.datetime);
  const params = {
    title: data.title?.trim(),
    eventName: data.title?.trim(),
    eventDate: date,
    eventTime: time,
    advanceNotice: String(parseInt(data.advanceNotice || '30', 10) || 30)
  };
  if (data.datetime) params.datetime = data.datetime;
  if (data.email?.trim()) params.email = data.email.trim();
  if (data.repeatDaily) params.repeatDaily = true;
  if (data.phoneNumber?.trim()) params.phoneNumber = data.phoneNumber.trim();
  const descParts = [];
  if (data.location?.trim()) descParts.push('地点：' + data.location.trim());
  if (data.notes?.trim()) descParts.push(data.notes.trim());
  if (descParts.length) params.description = descParts.join('\n');
  return params;
}

async function commitTravelReminderViaMcp(data) {
  const { data: res } = await apiService.confirmTravelReminder(buildTravelReminderMcpParams(data));
  return res;
}

function buildHotelBookMcpParams(data) {
  return {
    hotelName: data.hotelName,
    checkInDate: data.checkInDate,
    checkOutDate: data.checkOutDate,
    guests: data.guests ?? 2,
    contactName: data.contactName?.trim(),
    contactPhone: data.contactPhone?.trim(),
    contactEmail: data.contactEmail?.trim() || '',
    specialRequests: data.specialRequests?.trim() || ''
  };
}

async function commitHotelBookViaMcp(data) {
  const { data: res } = await apiService.confirmHotelBook(buildHotelBookMcpParams(data));
  return res;
}

function showHotelBookingForm(payload) {
  const hotelName = payload.hotelName || payload.name || '所选酒店';
  messages.value.push({
    role: 'function_call',
    functionName: 'hotel_booking_form',
    showBookingForm: true,
    bookingHotel: { hotelName, name: hotelName, address: payload.address || '' },
    bookingPrefill: {
      checkInDate: payload.checkInDate || '',
      checkOutDate: payload.checkOutDate || '',
      guests: payload.guests != null ? String(payload.guests) : '2',
      contactName: payload.contactName || '',
      contactPhone: payload.contactPhone || '',
      contactEmail: payload.contactEmail || '',
      specialRequests: payload.specialRequests || ''
    }
  });
}

const handleLearningReminderFormCancel = (formMsg) => {
  messages.value = messages.value.filter((m) => m !== formMsg);
};

const handleLearningReminderFormSubmit = async (formMsg, data) => {
  if (isLoading.value) return;
  const { date, time } = splitDatetimeLocalForApi(data.datetime);
  if (!data.title?.trim() || !date || !time) return;
  const emailTrim = data.email?.trim() || '';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrim)) return;

  isLoading.value = true;
  messages.value = messages.value.filter((m) => m !== formMsg);
  const userLine =
    `【学习提醒】${data.title.trim()} · ${date} ${time}` +
    (data.location?.trim() ? ` · ${data.location.trim()}` : '');
  messages.value.push({ role: 'user', content: userLine });
  scrollToBottom();

  try {
    const res = await commitTravelReminderViaMcp({ ...data, email: emailTrim });
    handleFunctionCall(res);
  } catch (e) {
    console.error(e);
    messages.value.push({
      role: 'assistant',
      content: '保存提醒时有点小波折，可能是网络或服务正在忙，等会儿再试一次好不好？'
    });
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

const handleEmailReminderFormCancel = (formMsg) => {
  messages.value = messages.value.filter((m) => m !== formMsg);
};

const handleUserProfileFormCancel = (formMsg) => {
  messages.value = messages.value.filter((m) => m !== formMsg);
};

const handleUserProfileFormSubmit = async (formMsg, data) => {
  if (isLoading.value) return;
  isLoading.value = true;
  messages.value = messages.value.filter((m) => m !== formMsg);
  const parts = [];
  if (data.realName) parts.push(data.realName);
  if (data.email) parts.push(data.email);
  if (data.phone) parts.push(data.phone);
  messages.value.push({
    role: 'user',
    content: `【保存用户信息】${parts.length ? parts.join(' · ') : '已填写表单'}`
  });
  scrollToBottom();

  try {
    const { data: res } = await apiService.saveUserProfile(data);
    if (res && res.ok === true) {
      messages.value.push({
        role: 'function_call',
        functionName: 'user_profile_saved',
        userProfileSaved: {
          id: res.id,
          message: res.message || '写入成功'
        }
      });
    } else {
      messages.value.push({
        role: 'assistant',
        content: (res && res.message) || '没有写入成功，可能是邮箱或手机号与已有记录重复，改一下再试？'
      });
    }
  } catch (e) {
    console.error(e);
    messages.value.push({
      role: 'assistant',
      content: '保存信息时有点小波折，可能是网络或服务正在忙，等会儿再试一次好不好？'
    });
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

const handleEmailReminderFormSubmit = async (formMsg, data) => {
  if (isLoading.value) return;
  if (!data.recipient?.trim() || !data.subject?.trim() || !data.content?.trim()) return;

  isLoading.value = true;
  messages.value = messages.value.filter((m) => m !== formMsg);
  const userLine = `【邮件提醒】${data.recipient.trim()} · ${data.subject.trim()}`;
  messages.value.push({ role: 'user', content: userLine });
  scrollToBottom();

  try {
    await apiService.sendEmail({
      recipient: data.recipient.trim(),
      subject: data.subject.trim(),
      content: data.content.trim()
    });
    const raw = data.content.trim();
    const bodyPreview = raw.length > 160 ? `${raw.slice(0, 160)}…` : raw;
    messages.value.push({
      role: 'function_call',
      functionName: 'email_reminder_sent',
      emailReminderSaved: {
        recipient: data.recipient.trim(),
        subject: data.subject.trim(),
        bodyPreview
      }
    });
  } catch (e) {
    console.error(e);
    messages.value.push({
      role: 'assistant',
      content: '发送邮件时有点小波折，可能是网络或服务正在忙，等会儿再试一次好不好？'
    });
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

function escapeHtml(s) {
  if (s == null) return '';
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function inlineMdBold(s) {
  return s.replace(/\*\*(.+?)\*\*/g, '<strong class="ar-strong">$1</strong>');
}

/** 智学伴普通气泡：把标题、列表、粗体等 Markdown 片段转成易读 HTML，避免满屏符号 */
function renderAssistantStudyHtml(raw) {
  if (raw == null || raw === '') return '';
  const lines = String(raw).replace(/\r\n/g, '\n').split('\n');
  const parts = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const trimmed = line.trim();
    if (trimmed === '') {
      parts.push('<div class="ar-gap" aria-hidden="true"></div>');
      continue;
    }
    const esc = escapeHtml(line);
    let m = esc.match(/^#{1,4}\s+(.+)$/);
    if (m) {
      parts.push(`<div class="ar-block ar-h">${inlineMdBold(m[1].trim())}</div>`);
      continue;
    }
    m = esc.match(/^[\-*]\s+(.+)$/);
    if (m) {
      parts.push(`<div class="ar-block ar-li">${inlineMdBold(m[1])}</div>`);
      continue;
    }
    m = esc.match(/^\d+\.\s+(.+)$/);
    if (m) {
      parts.push(`<div class="ar-block ar-li ar-num">${inlineMdBold(m[1])}</div>`);
      continue;
    }
    parts.push(`<div class="ar-block ar-p">${inlineMdBold(esc)}</div>`);
  }
  return parts.join('');
}

/** 后端把 API 错误包装成 200 + 正文时，仍应显示「再试一次」 */
function isReviewRhythmLlmFailureText(text) {
  if (!text || typeof text !== 'string') return false;
  const t = text.trim();
  if (/#{2,4}\s/.test(t) && t.length > 160) return false;
  return /429|限流|模型接口刚回了|模型服务那边|连麦没连上|解析响应出错|未识别模型返回格式|密钥、额度|请求太密|TPM|RPM|Too Many Requests/i.test(
    t
  );
}

function buildReviewRhythmPrompt(data) {
  const focusLine =
    data.focusNote && String(data.focusNote).trim()
      ? `侧重点：${String(data.focusNote).trim()}\n`
      : '侧重点：\n';
  return (
    `【复习节奏表单】\n` +
    `科目/主题：${data.subject}\n` +
    `备考天数：${data.days}\n` +
    `每天可学习：约 ${data.dailyHours} 小时\n` +
    `当前基础：${data.level}\n` +
    focusLine
  );
}

const handleReviewRhythmFormCancel = (formMsg) => {
  messages.value = messages.value.filter((m) => m !== formMsg);
};

const handleReviewPlanFormCancel = (formMsg) => {
  messages.value = messages.value.filter((m) => m !== formMsg);
};

const handleReviewRhythmFormSubmit = async (formMsg, data) => {
  if (isLoading.value) return;
  messages.value = messages.value.filter((m) => m !== formMsg);
  const summary =
    `【复习节奏】${data.subject} · ${data.days} 天 · 每天约 ${data.dailyHours} 小时 · ${data.level}` +
    (data.focusNote ? ` · ${data.focusNote}` : '');
  messages.value.push({ role: 'user', content: summary });
  scrollToBottom();
  const prompt = buildReviewRhythmPrompt(data);
  await streamChatResponse(prompt, {
    reviewPlanCard: true,
    reviewRhythmPrompt: prompt,
    reviewPlanVariant: 'rhythm'
  });
};

const handleReviewPlanFormSubmit = async (formMsg, data) => {
  if (isLoading.value) return;
  messages.value = messages.value.filter((m) => m !== formMsg);
  const summary =
    `【复习计划】${data.subject} · ${data.days} 天 · 每天约 ${data.dailyHours} 小时 · ${data.level}` +
    (data.focusNote ? ` · ${data.focusNote}` : '');
  messages.value.push({ role: 'user', content: summary });
  scrollToBottom();
  const prompt = buildReviewRhythmPrompt(data);
  await streamChatResponse(prompt, {
    reviewPlanCard: true,
    reviewRhythmPrompt: prompt,
    reviewPlanVariant: 'plan'
  });
};

const handleReviewRhythmRetry = async (msg) => {
  if (isLoading.value || !msg?.reviewRhythmPrompt) return;
  const idx = messages.value.indexOf(msg);
  if (idx < 0) return;
  await streamChatResponse(msg.reviewRhythmPrompt, {
    reviewPlanCard: true,
    reviewRhythmPrompt: msg.reviewRhythmPrompt,
    existingAssistantIndex: idx,
    reviewPlanVariant: msg.reviewPlanVariant === 'plan' ? 'plan' : 'rhythm'
  });
};

/**
 * @param {string} promptText
 * @param {{
 *   reviewPlanCard?: boolean;
 *   reviewRhythmPrompt?: string | null;
 *   existingAssistantIndex?: number;
 *   reviewPlanVariant?: 'rhythm' | 'plan';
 * }} opts
 */
const streamChatResponse = async (promptText, opts = {}) => {
  const streamId = ++activeStreamId;
  const reviewPlanCard = !!opts.reviewPlanCard;
  const profileInsertCtaEligible = wantsUserProfileInsertHint(promptText) && !reviewPlanCard;
  const reviewPlanVariant =
    reviewPlanCard && opts.reviewPlanVariant === 'plan' ? 'plan' : reviewPlanCard ? 'rhythm' : undefined;
  const savedPrompt = opts.reviewRhythmPrompt != null ? opts.reviewRhythmPrompt : null;
  isLoading.value = true;

  const existingIdx = opts.existingAssistantIndex;
  let assistantIdx;
  if (
    typeof existingIdx === 'number' &&
    existingIdx >= 0 &&
    existingIdx < messages.value.length
  ) {
    assistantIdx = existingIdx;
    const m = messages.value[assistantIdx];
    m.content = '';
    m.isStreaming = true;
    m.isWeatherResult = false;
    m.weatherCardTitle = '';
    m.weatherCardHint = '';
    m.weatherRaw = '';
    m.isReviewRhythmPlan = true;
    m.reviewRhythmPrompt = savedPrompt || m.reviewRhythmPrompt || null;
    m.reviewRhythmGenerationOk = false;
    m.reviewPlanVariant = reviewPlanVariant ?? m.reviewPlanVariant ?? 'rhythm';
    m.profileInsertCtaEligible = profileInsertCtaEligible;
    m.showUserProfileInsertCta = false;
    m.profileInsertFormOpened = false;
  } else {
    assistantIdx = messages.value.length;
    messages.value.push({
      role: 'assistant',
      content: '',
      isStreaming: true,
      isWeatherResult: false,
      weatherCardTitle: '',
      weatherCardHint: '',
      weatherRaw: '',
      isReviewRhythmPlan: reviewPlanCard,
      reviewPlanVariant,
      reviewRhythmPrompt: savedPrompt,
      reviewRhythmGenerationOk: false,
      profileInsertCtaEligible,
      showUserProfileInsertCta: false,
      profileInsertFormOpened: false
    });
  }

  if (!getToken()) {
    messages.value[assistantIdx].content = '请先登录后再使用对话功能。';
    messages.value[assistantIdx].isStreaming = false;
    isLoading.value = false;
    return;
  }

  const ctrl = new AbortController();
  activeAbortController = ctrl;

  try {
    await fetchEventSource(`${API_BASE}${CHAT_SSE_PATH}`, {
      method: 'POST',
      signal: ctrl.signal,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        Authorization: `Bearer ${getToken()}`,
      },
      body: JSON.stringify(
        props.teacherMode
          ? { message: promptText, teacherMode: true }
          : promptText,
      ),
      maxRetries: 0,

      onopen(res) {
        if (res.status === 401) {
          throw new Error('AUTH_401');
        }
        if (!res.ok) {
          throw new Error(`HTTP_${res.status}`);
        }
        const ct = res.headers.get('content-type') || '';
        if (!ct.includes('text/event-stream')) {
          throw new Error('NOT_SSE');
        }
      },

      onmessage({ event, data }) {
        try {
          // SSE 在 CRLF 下解析出的 event 常带尾 \r，会导致与 'direct_answer' 等分支对不上
          const ev = String(event ?? '')
            .replace(/\r/g, '')
            .trim();
          const raw = typeof data === 'string' ? data.replace(/\r\n/g, '\n') : data;

          let parsed = raw;
          try {
            parsed = JSON.parse(raw);
          } catch (_) {
            // direct_answer 等事件可能是纯文本，不是 JSON
          }
          console.log('收到事件:', ev, parsed);

          switch (ev) {
            case 'weather_start':
              messages.value[assistantIdx].isWeatherResult = true;
              messages.value[assistantIdx].content = '';
              if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
                messages.value[assistantIdx].weatherCardTitle =
                  parsed.cardTitle != null ? String(parsed.cardTitle) : '';
                messages.value[assistantIdx].weatherCardHint =
                  parsed.cardHint != null ? String(parsed.cardHint) : '';
                messages.value[assistantIdx].weatherRaw =
                  parsed.weather_raw != null ? String(parsed.weather_raw) : '';
              }
              break;

            case 'weather_chunk':
              if (parsed.content) {
                messages.value[assistantIdx].content += parsed.content;
                scrollToBottom();
              }
              break;

            case 'weather_complete':
              messages.value[assistantIdx].isStreaming = false;
              break;

            case 'function_result':
              handleFunctionCall(parsed);
              messages.value.splice(assistantIdx, 1);
              break;

            case 'generation_start':
              if (messages.value[assistantIdx]) {
                messages.value[assistantIdx].llmGenerationStarted = true;
              }
              break;

            case 'direct_answer_chunk':
              if (parsed && typeof parsed === 'object' && parsed.content != null) {
                messages.value[assistantIdx].content += String(parsed.content);
                scrollToBottom();
              }
              break;

            case 'direct_answer_done': {
              const mDone = messages.value[assistantIdx];
              if (!mDone) break;
              mDone.isStreaming = false;
              const strDone = typeof mDone.content === 'string' ? mDone.content : '';
              if (mDone.isReviewRhythmPlan && isReviewRhythmLlmFailureText(strDone)) {
                mDone.reviewRhythmGenerationOk = false;
              } else {
                mDone.reviewRhythmGenerationOk = true;
              }
              finalizeProfileInsertCta(assistantIdx);
              scrollToBottom();
              break;
            }

            case 'direct_answer':
              if (parsed !== undefined && parsed !== null) {
                const str =
                  typeof parsed === 'string'
                    ? parsed
                    : typeof parsed === 'object'
                      ? JSON.stringify(parsed, null, 2)
                      : String(parsed);
                messages.value[assistantIdx].content = str;
                messages.value[assistantIdx].isStreaming = false;
                const m = messages.value[assistantIdx];
                if (m.isReviewRhythmPlan && isReviewRhythmLlmFailureText(str)) {
                  m.reviewRhythmGenerationOk = false;
                } else {
                  m.reviewRhythmGenerationOk = true;
                }
                finalizeProfileInsertCta(assistantIdx);
                scrollToBottom();
              }
              break;

            case 'error': {
              const errText =
                typeof parsed === 'string' && parsed.trim()
                  ? parsed.trim()
                  : '哎呀，这边有点小波折…你可以再说一遍，我认真听。';
              messages.value[assistantIdx].content = errText;
              messages.value[assistantIdx].isStreaming = false;
              messages.value[assistantIdx].reviewRhythmGenerationOk = false;
              break;
            }

            default:
              console.warn('未知事件类型:', ev, raw);
              break;
          }
        } catch (e) {
          console.error('处理消息错误:', e);
        }
      },

      onclose() {
        if (streamId !== activeStreamId) return;
        const m = messages.value[assistantIdx];
        if (m) {
          m.isStreaming = false;
          if (
            m.isReviewRhythmPlan &&
            m.reviewRhythmGenerationOk !== true &&
            typeof m.content === 'string'
          ) {
            const t = m.content.trim();
            if (
              t.length >= 80 &&
              /#{1,3}\s+\S/m.test(t) &&
              !isReviewRhythmLlmFailureText(t)
            ) {
              m.reviewRhythmGenerationOk = true;
            }
          }
        }
        finalizeProfileInsertCta(assistantIdx);
        isLoading.value = false;
      },

      onerror(err) {
        if (streamId !== activeStreamId) {
          throw err;
        }
        if (ctrl.signal.aborted || err?.name === 'AbortError') {
          isLoading.value = false;
          return;
        }
        console.error('SSE 错误', err);
        const msg = String(err?.message || err || '');
        if (msg === 'AUTH_401' || msg.includes('401')) {
          messages.value[assistantIdx].content =
            '登录已过期，请退出后重新登录再试。';
        } else if (
          msg.includes('Failed to fetch') ||
          msg.includes('NetworkError') ||
          msg.includes('ECONNREFUSED')
        ) {
          messages.value[assistantIdx].content =
            `无法连接后端服务（${API_BASE || '当前站点'}）。请确认 NeuLLMDev 已启动（默认端口 8082），再重试。`;
        } else {
          messages.value[assistantIdx].content =
            '对话连接中断。若刚点过多次发送，请等 1～2 分钟再试（模型可能限流 429）。';
        }
        messages.value[assistantIdx].isStreaming = false;
        messages.value[assistantIdx].reviewRhythmGenerationOk = false;
        isLoading.value = false;
        throw err;
      }
    });
  } catch (e) {
    if (streamId !== activeStreamId) return;
    if (ctrl.signal.aborted || e?.name === 'AbortError') {
      isLoading.value = false;
      return;
    }
    console.error('网络/解析错误', e);
    if (messages.value[assistantIdx]?.isStreaming) {
      messages.value[assistantIdx].content =
        '咦，好像和网络暂时失联了……等你方便的时候再试一下，我在这儿等你。';
      messages.value[assistantIdx].isStreaming = false;
    }
    isLoading.value = false;
  } finally {
    if (activeAbortController === ctrl) {
      activeAbortController = null;
    }
  }
};

// 取消旅行提醒
const cancelTravelReminder = (message) => {
  messages.value = messages.value.filter(m => m !== message);
};

// 酒店预订触发 - 直接显示预订表单
const handleHotelBook = (hotel) => {
  // 直接显示预订表单，不需要通过 LLM
  messages.value.push({
    role: 'function_call',
    functionName: 'hotel_booking_form',
    showBookingForm: true,
    bookingHotel: hotel
  });
  scrollToBottom();
};

// 取消酒店预订
const cancelHotelBooking = (message) => {
  messages.value = messages.value.filter(m => m !== message);
};

// 提交酒店预订（MCP hotel_book committed）
const submitHotelBooking = async (bookingData) => {
  if (isLoading.value) return;
  isLoading.value = true;

  const bookingMsg = messages.value.find(m => m.showBookingForm);
  if (bookingMsg) {
    messages.value = messages.value.filter(m => m !== bookingMsg);
  }

  const loadingMsg = {
    role: 'assistant',
    content: '正在提交预订…',
    isStreaming: true
  };
  messages.value.push(loadingMsg);
  scrollToBottom();

  try {
    const res = await commitHotelBookViaMcp(bookingData);
    messages.value = messages.value.filter(m => m !== loadingMsg);
    handleFunctionCall(res);
  } catch (error) {
    console.error('酒店预订失败:', error);
    loadingMsg.content = '预订请求失败，请稍后重试';
    loadingMsg.isStreaming = false;
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 计算晚数
const calculateNights = (checkIn, checkOut) => {
  const d1 = new Date(checkIn);
  const d2 = new Date(checkOut);
  return Math.ceil((d2 - d1) / (1000 * 60 * 60 * 24));
};

// 保存旅行提醒（卡片确认 → MCP committed）
const saveTravelReminder = async (message, reminder) => {
  if (isLoading.value) return;
  isLoading.value = true;
  try {
    const res = await commitTravelReminderViaMcp(reminder);
    handleFunctionCall(res);
  } catch (error) {
    console.error('保存旅行提醒失败:', error);
    messages.value.push({
      role: 'assistant',
      content: '保存提醒时有点小波折，等会儿再试一次好不好？'
    });
    scrollToBottom();
  } finally {
    isLoading.value = false;
  }
};

async function loadMessagesSummaryIntoMessage(message) {
  message.messagesSummaryLoading = true;
  message.messagesSummaryError = '';
  isLoading.value = true;
  try {
    const data = await notificationsApi.summary(20);
    message.messagesSummaryData = {
      summary: data?.summary || '',
      unread: data?.unread ?? 0,
      unreadItems: Array.isArray(data?.unreadItems) ? data.unreadItems : [],
      empty: !!data?.empty,
      allCaughtUp: !!data?.allCaughtUp,
      total: data?.total ?? 0,
    };
    message.content = message.messagesSummaryData.summary;
  } catch (e) {
    message.messagesSummaryError = e?.message || '消息加载失败，请稍后再试';
  } finally {
    message.messagesSummaryLoading = false;
    isLoading.value = false;
    scrollToBottom();
  }
}

async function retryMessagesSummary(message) {
  await loadMessagesSummaryIntoMessage(message);
}

async function loadAssignmentsSummaryIntoMessage(message) {
  message.assignmentsSummaryLoading = true;
  message.assignmentsSummaryError = '';
  isLoading.value = true;
  try {
    const data = await classroomApi.myAssignmentsSummary();
    message.assignmentsSummaryData = {
      summary: data?.summary || '',
      pendingCount: data?.pendingCount ?? 0,
      totalTasks: data?.totalTasks ?? 0,
      pendingItems: Array.isArray(data?.pendingItems) ? data.pendingItems : [],
      empty: !!data?.empty,
    };
    message.content = message.assignmentsSummaryData.summary;
  } catch (e) {
    message.assignmentsSummaryError = e?.message || '作业查询失败，请稍后再试';
  } finally {
    message.assignmentsSummaryLoading = false;
    isLoading.value = false;
    scrollToBottom();
  }
}

async function retryAssignmentsSummary(message) {
  await loadAssignmentsSummaryIntoMessage(message);
}

async function loadTeacherSubmissionsSummaryIntoMessage(message) {
  message.teacherSubmissionsSummaryLoading = true;
  message.teacherSubmissionsSummaryError = '';
  isLoading.value = true;
  try {
    const data = await classroomApi.recentSubmissionsSummary();
    message.teacherSubmissionsSummaryData = {
      summary: data?.summary || '',
      pendingCount: data?.pendingCount ?? 0,
      totalSubmissions: data?.totalSubmissions ?? 0,
      pendingItems: Array.isArray(data?.pendingItems) ? data.pendingItems : [],
      empty: !!data?.empty,
    };
    message.content = message.teacherSubmissionsSummaryData.summary;
  } catch (e) {
    message.teacherSubmissionsSummaryError = e?.message || '提交汇总失败，请稍后再试';
  } finally {
    message.teacherSubmissionsSummaryLoading = false;
    isLoading.value = false;
    scrollToBottom();
  }
}

async function retryTeacherSubmissionsSummary(message) {
  await loadTeacherSubmissionsSummaryIntoMessage(message);
}

async function loadTeacherClassInsightSummaryIntoMessage(message, insightIntent, question) {
  message.teacherClassInsightSummaryLoading = true;
  message.teacherClassInsightSummaryError = '';
  isLoading.value = true;
  try {
    const data = await classroomApi.classInsightSummary({
      focus: insightIntent?.focus,
      question,
    });
    message.teacherClassInsightSummaryData = {
      summary: data?.summary || '',
      focus: data?.focus || insightIntent?.focus || 'general',
      focusLabel: data?.focusLabel || insightIntent?.label || '班级学情',
      classCompletionPercent: data?.classCompletionPercent ?? null,
      highlightStudents: Array.isArray(data?.highlightStudents) ? data.highlightStudents : [],
      latestTaskId: data?.latestTaskId || '',
      empty: !!data?.empty,
    };
    message.content = message.teacherClassInsightSummaryData.summary;
  } catch (e) {
    message.teacherClassInsightSummaryError = e?.message || '学情汇总失败，请稍后再试';
  } finally {
    message.teacherClassInsightSummaryLoading = false;
    isLoading.value = false;
    scrollToBottom();
  }
}

async function retryTeacherClassInsightSummary(message) {
  await loadTeacherClassInsightSummaryIntoMessage(
    message,
    { focus: message.teacherClassInsightSummaryData?.focus, label: message.teacherClassInsightSummaryData?.focusLabel },
    message.teacherClassInsightQuestion || '',
  );
}

const sendMessage = async () => {
  if (isLoading.value || !userInput.value.trim()) return;

  const inputText = userInput.value.trim();
  userInput.value = '';
  dismissStaleInlineForms();

  messages.value.push({ role: 'user', content: inputText });
  scrollToBottom();

  const classInsightIntent = props.teacherMode ? detectTeacherClassInsight(inputText) : null;
  if (classInsightIntent) {
    const summaryMsg = {
      role: 'assistant',
      content: '',
      isTeacherClassInsightSummary: true,
      teacherClassInsightSummaryLoading: true,
      teacherClassInsightSummaryError: '',
      teacherClassInsightSummaryData: null,
      teacherClassInsightQuestion: inputText,
    };
    messages.value.push(summaryMsg);
    scrollToBottom();
    await loadTeacherClassInsightSummaryIntoMessage(summaryMsg, classInsightIntent, inputText);
    return;
  }

  if (props.teacherMode && wantsTeacherSubmissionsSummary(inputText)) {
    const summaryMsg = {
      role: 'assistant',
      content: '',
      isTeacherSubmissionsSummary: true,
      teacherSubmissionsSummaryLoading: true,
      teacherSubmissionsSummaryError: '',
      teacherSubmissionsSummaryData: null,
    };
    messages.value.push(summaryMsg);
    scrollToBottom();
    await loadTeacherSubmissionsSummaryIntoMessage(summaryMsg);
    return;
  }

  if (!props.teacherMode && wantsAssignmentSummary(inputText)) {
    const summaryMsg = {
      role: 'assistant',
      content: '',
      isAssignmentsSummary: true,
      assignmentsSummaryLoading: true,
      assignmentsSummaryError: '',
      assignmentsSummaryData: null,
    };
    messages.value.push(summaryMsg);
    scrollToBottom();
    await loadAssignmentsSummaryIntoMessage(summaryMsg);
    return;
  }

  if (!props.teacherMode && wantsMessagesSummary(inputText)) {
    const summaryMsg = {
      role: 'assistant',
      content: '',
      isMessagesSummary: true,
      messagesSummaryLoading: true,
      messagesSummaryError: '',
      messagesSummaryData: null,
    };
    messages.value.push(summaryMsg);
    scrollToBottom();
    await loadMessagesSummaryIntoMessage(summaryMsg);
    return;
  }

  const navTarget = detectAppNavIntent(inputText, { teacherMode: props.teacherMode });
  if (navTarget) {
    messages.value.push({
      role: 'assistant',
      content: `好的，点下面按钮就能进入${navTarget.label}。`,
      isAppNavJump: true,
      appNavTarget: navTarget,
    });
    scrollToBottom();
    return;
  }

  if (wantsReviewRhythmCard(inputText)) {
    messages.value.push({
      role: 'assistant',
      content: '',
      isReviewRhythmForm: true
    });
    scrollToBottom();
    return;
  }

  if (wantsLearningReminderCard(inputText)) {
    messages.value.push({
      role: 'assistant',
      content: '',
      isLearningReminderForm: true,
      reminderFormPrefill: parseReminderPrefill(inputText)
    });
    scrollToBottom();
    return;
  }

  if (wantsUserProfileInsertHint(inputText)) {
    const prefill = tryExtractProfilePrefillFromPlainText(inputText);
    messages.value.push({
      role: 'assistant',
      content: '',
      isUserProfileForm: true,
      userProfileFormPrefill: Object.keys(prefill).length ? prefill : null
    });
    scrollToBottom();
    return;
  }

  if (wantsEmailReminderCard(inputText)) {
    const emailFormPrefill = parseEmailReminderPrefill(inputText);
    const nameHint = extractRecipientNameForEmail(inputText);
    if (nameHint && !emailFormPrefill.recipient) {
      try {
        const { data: lu } = await apiService.lookupUserProfile(nameHint);
        if (lu && lu.ok === true && lu.email) {
          emailFormPrefill.recipient = String(lu.email).trim();
        }
      } catch (e) {
        console.warn('lookupUserProfile failed', e);
      }
    }
    messages.value.push({
      role: 'assistant',
      content: '',
      isEmailReminderForm: true,
      emailFormPrefill
    });
    scrollToBottom();
    return;
  }

  if (wantsReviewPlanCard(inputText)) {
    if (!hasReviewPlanRichContext(inputText)) {
      messages.value.push({
        role: 'assistant',
        content: '',
        isReviewPlanForm: true
      });
      scrollToBottom();
      return;
    }
    await streamChatResponse(inputText, {
      reviewPlanCard: true,
      reviewRhythmPrompt: inputText,
      reviewPlanVariant: 'plan'
    });
    return;
  }

  await streamChatResponse(inputText, { reviewPlanCard: false });
};

function normalizeClassroomData(data, kind) {
  if (data == null) return null;
  if (typeof data === 'string') {
    try {
      return JSON.parse(data);
    } catch {
      return data;
    }
  }
  if (kind === 'classroom_submissions' || kind === 'classroom_students') {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.items)) return data.items;
  }
  return data;
}

// 处理函数调用（function_result 里通常是工具返回值：字符串 JSON 或对象）
const handleFunctionCall = (call) => {
  console.log('处理函数调用:', call);

  let payload = call;
  if (typeof call === 'string') {
    try {
      payload = JSON.parse(call);
    } catch {
      messages.value.push({
        role: 'assistant',
        content: typeof call === 'string' ? call.replace(/\\n/g, '\n') : String(call)
      });
      scrollToBottom();
      return;
    }
  }

  if (payload && payload.displayKind === 'classroom_error') {
    messages.value.push({
      role: 'assistant',
      content: payload.message || '课堂任务执行失败，请检查是否已用教师账号登录，或补充 taskId / submissionId 后重试。',
    });
    scrollToBottom();
    return;
  }

  if (
    payload.displayKind === 'classroom_task_preview' ||
    payload.displayKind === 'classroom_publish_preview'
  ) {
    showTaskPreviewCard(
      payload.displayKind,
      payload.preview || {},
      payload.summary || '',
    );
    return;
  }

  if (payload && typeof payload.displayKind === 'string' && payload.displayKind.startsWith('classroom_')) {
    messages.value.push({
      role: 'function_call',
      functionName: payload.functionName || payload.displayKind,
      classroomResult: {
        kind: payload.displayKind,
        summary: payload.summary || '',
        data: normalizeClassroomData(payload.data, payload.displayKind),
      },
    });
    scrollToBottom();
    return;
  }

  if (payload && payload.displayKind === 'reminder_saved') {
    messages.value.push({
      role: 'function_call',
      functionName: 'reminder_saved',
      reminderSaved: payload
    });
    scrollToBottom();
    return;
  }
  if (payload && payload.displayKind === 'reminder_error') {
    messages.value.push({
      role: 'assistant',
      content: payload.message || '提醒这块我有点没办好…你再跟我说一遍时间好不好？'
    });
    scrollToBottom();
    return;
  }

  // 根据后端返回的数据结构处理
  if (payload.functionName === 'send_email' || payload.name === 'send_email' || payload.function === 'send_email') {
    const params = payload.params && typeof payload.params === 'object' ? payload.params : {};
    const recipient = (payload.recipient ?? params.recipient ?? '').toString().trim();
    const subjectRaw = payload.subject ?? params.subject;
    const subject =
      subjectRaw != null && String(subjectRaw).trim() !== '' ? String(subjectRaw).trim() : '无主题';
    const content = (payload.content ?? params.content ?? '').toString();
    messages.value.push({
      role: 'assistant',
      content: '',
      isEmailReminderForm: true,
      emailFormPrefill: {
        recipient,
        subject,
        content
      }
    });
  } else if (payload.functionName === 'save_user_profile' || payload.name === 'save_user_profile') {
    const params = payload.params && typeof payload.params === 'object' ? payload.params : {};
    const pick = (kSnake, kCamel) =>
      (payload[kCamel] ?? payload[kSnake] ?? params[kCamel] ?? params[kSnake] ?? '').toString().trim();
    messages.value.push({
      role: 'assistant',
      content: '',
      isUserProfileForm: true,
      userProfileFormPrefill: {
        externalId: pick('external_id', 'externalId'),
        displayName: pick('display_name', 'displayName'),
        realName: pick('real_name', 'realName'),
        email: pick('email', 'email'),
        phone: pick('phone', 'phone'),
        address: pick('address', 'address'),
        gender: pick('gender', 'gender'),
        avatarUrl: pick('avatar_url', 'avatarUrl'),
        remark: pick('remark', 'remark')
      }
    });
  } else if (payload.functionName === 'getLocationInfo' || payload.name === 'getLocationInfo') {
    const functionCall = payload;
    messages.value.push({
      role: 'function_call',
      functionName: 'getLocationInfo',
      origin: functionCall.origin,
      destination: functionCall.destination,
      mapVisible: true
    });
  } else if (payload.functionName === 'food_search' || payload.name === 'food_search') {
    messages.value.push({
      role: 'function_call',
      functionName: 'food_search',
      foodData: payload
    });
  } else if (payload.functionName === 'hotel_recommend' || payload.name === 'hotel_recommend' || payload.hotels || payload.hotelList) {
    console.log('🏨 酒店推荐结果:', payload);
    messages.value.push({
      role: 'function_call',
      functionName: 'hotel_recommend',
      hotelData: payload
    });

  } else if (payload.displayKind === 'hotel_book_preview') {
    showHotelBookingForm(payload);
  } else if (
    (payload.functionName === 'hotel_book' || payload.name === 'hotel_book') &&
    payload.status === 'success' &&
    payload.orderId
  ) {
    console.log('📋 酒店预订结果:', payload);
    messages.value.push({
      role: 'function_call',
      functionName: 'hotel_book',
      hotelBookData: payload
    });
  } else if (payload.functionName === 'hotel_book' || payload.name === 'hotel_book') {
    if (payload.status === 'error') {
      messages.value.push({
        role: 'assistant',
        content: payload.message || '预订信息不完整，请补充联系人、日期后再试。'
      });
    } else {
      showHotelBookingForm(payload);
    }

  } else if (payload.functionName === 'recommend_destination' || payload.name === 'recommend_destination') {
    const recommendationText = (payload.recommendations || [])
        .slice(0, 3)
        .map((item, idx) => `${idx + 1}. ${item.destination}：${item.reason}（建议行程：${item.suggestedPlan}）`)
        .join('\n');

    messages.value.push({
      role: 'function_call',
      functionName: 'recommend_destination',
      content: `${payload.summary || '已生成目的地推荐。'}\n${recommendationText}`
    });
  } else if (payload.functionName === 'get_current_weather' || payload.name === 'get_current_weather') {
    console.log('天气函数调用:', payload);
  } else if (
    payload.functionName === 'setTravelReminder' || payload.name === 'setTravelReminder'
    || payload.functionName === 'create_travel_reminder' || payload.name === 'create_travel_reminder'
  ) {
    const fn = (payload.functionName || payload.name || 'create_travel_reminder');
    messages.value.push({
      role: 'function_call',
      functionName: fn === 'setTravelReminder' ? 'setTravelReminder' : 'create_travel_reminder',
      reminderData: {
        title: payload.title || '',
        location: payload.location || '',
        datetime: payload.datetime || '',
        notes: payload.notes || '',
        advanceNotice: payload.advanceNotice || '30',
        email: payload.email || ''
      }
    });
  } else if (payload.functionName === 'plan_itinerary' || payload.name === 'plan_itinerary' || (payload.dailyPlans && payload.destination)) {
    messages.value.push({
      role: 'function_call',
      functionName: 'plan_itinerary',
      content: payload.summary || '',
      planData: {
        destination: payload.destination || '',
        days: payload.days || 3,
        totalBudget: payload.totalBudget || 0,
        center: payload.center || '',
        dailyPlans: payload.dailyPlans || []
      }
    });
  } else {
    messages.value.push({
      role: 'assistant',
      content: typeof payload === 'object'
        ? ('收到工具返回结果：\n```\n' + JSON.stringify(payload, null, 2) + '\n```')
        : String(payload)
    });
  }
  scrollToBottom();
};

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    nextTick(() => {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    });
  }
};

function isInlineFormMessage(message) {
  return !!(
    message.isUserProfileForm ||
    message.isEmailReminderForm ||
    message.isLearningReminderForm ||
    message.isReviewRhythmForm ||
    message.isAppNavJump ||
    message.isMessagesSummary ||
    message.isAssignmentsSummary ||
    message.isTeacherClassInsightSummary ||
    message.isTeacherSubmissionsSummary ||
    message.classroomResult ||
    message.isClassroomTaskConfirm ||
    message.isClassroomDocImport
  );
}

function dismissStaleInlineForms() {
  messages.value = messages.value.filter(
    (m) =>
      !m.isUserProfileForm &&
      !m.isLearningReminderForm &&
      !m.isEmailReminderForm &&
      !m.isReviewRhythmForm &&
      !m.isClassroomTaskConfirm &&
      !m.isClassroomDocImport
  );
  for (const m of messages.value) {
    if (m.role === 'assistant') {
      m.profileInsertFormOpened = false;
      m.showUserProfileInsertCta = false;
    }
  }
}

onMounted(() => {
  syncTeacherMobileViewport();
  if (typeof window !== 'undefined') {
    teacherMobileMediaQuery = window.matchMedia(TEACHER_MOBILE_MQ);
    teacherMobileMediaQuery.addEventListener('change', syncTeacherMobileViewport);
  }
  scrollToBottom();
  refreshStudentUnread();
});

let chatPersistTimer = null;
watch(
  messages,
  (list) => {
    if (chatPersistTimer) clearTimeout(chatPersistTimer);
    chatPersistTimer = setTimeout(() => {
      saveChatSession(props.teacherMode, list);
    }, 250);
  },
  { deep: true },
);

onBeforeUnmount(() => {
  if (chatPersistTimer) clearTimeout(chatPersistTimer);
  saveChatSession(props.teacherMode, messages.value);
});

onUnmounted(() => {
  teacherMobileMediaQuery?.removeEventListener('change', syncTeacherMobileViewport);
});
</script>

<style>
@import '../assets/zhixueban-shell.css';
@import '../assets/chat-form-card.css';

/* 仅作用于聊天页根节点，避免污染教师后台等其它路由 */
.chat-page,
.chat-page * {
  box-sizing: border-box;
}

.chat-page {
  height: 100%;
  min-height: 0;
  font-family: var(--zx-font);
  color: var(--zx-text);
}

html.chat-route,
html.chat-route body,
html.chat-route #app {
  height: 100%;
  overflow: hidden;
}

html.chat-route body {
  margin: 0;
  padding: 0;
}

html.chat-route .chat-page {
  height: 100%;
  max-height: 100%;
  padding: 16px;
  background:
    var(--zx-gradient-mesh),
    var(--zx-shell-gradient);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.app-container {
  flex: 1;
  min-height: 0;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-page header:not(.s-agent-hero) {
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px) saturate(1.3);
  -webkit-backdrop-filter: blur(20px) saturate(1.3);
  border-radius: var(--zx-radius-lg);
  padding: 18px 22px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.14);
}

/* ── 学生端 Hero ── */
.s-agent-hero {
  position: relative;
  flex-shrink: 0;
  padding: 16px 20px 14px;
  border-radius: calc(var(--zx-radius-lg) + 2px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background:
    linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.14) 0%,
      rgba(255, 255, 255, 0.08) 45%,
      rgba(13, 148, 136, 0.12) 100%
    );
  backdrop-filter: blur(22px) saturate(1.35);
  -webkit-backdrop-filter: blur(22px) saturate(1.35);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.08) inset,
    0 8px 32px rgba(0, 0, 0, 0.18),
    0 2px 8px rgba(13, 148, 136, 0.12);
  overflow: hidden;
}

.s-agent-hero__glow {
  pointer-events: none;
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 50% 80% at 95% 10%, rgba(20, 184, 166, 0.22) 0%, transparent 58%),
    radial-gradient(ellipse 40% 60% at 5% 90%, rgba(34, 211, 238, 0.12) 0%, transparent 55%);
}

.s-agent-hero__top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.s-agent-hero__brand {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
}

.s-agent-hero__icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  font-size: 18px;
  color: #fff;
  background: linear-gradient(145deg, #0f766e 0%, #0d9488 48%, #14b8a6 100%);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.2) inset,
    0 4px 14px rgba(13, 148, 136, 0.35);
  flex-shrink: 0;
}

.s-agent-hero__copy {
  min-width: 0;
  text-align: left;
}

.s-agent-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #99f6e4;
  margin-bottom: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(13, 148, 136, 0.22);
  border: 1px solid rgba(153, 246, 228, 0.25);
}

.s-agent-hero__brand h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 1.45rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.15;
  color: #fff;
}

.s-agent-hero__brand p {
  margin: 5px 0 0;
  font-size: 0.82rem;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.78);
  max-width: 52ch;
}

.s-agent-hero__meta {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.s-agent-hero__live {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  color: #6ee7b7;
  background: rgba(16, 185, 129, 0.14);
  border: 1px solid rgba(110, 231, 183, 0.28);
}

.s-agent-hero__live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.22);
  animation: s-agent-live-pulse 2s ease-in-out infinite;
}

@keyframes s-agent-live-pulse {
  0%, 100% {
    box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.22);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(52, 211, 153, 0.08);
    transform: scale(1.08);
  }
}

.s-agent-hero__nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.s-nav-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.92);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.15s ease;
  font-family: inherit;
  cursor: pointer;
}

.s-nav-pill:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.32);
  transform: translateY(-1px);
}

.s-nav-pill--msg {
  position: relative;
}

.s-nav-pill--logout {
  background: rgba(255, 255, 255, 0.06);
}

.s-nav-pill i {
  font-size: 0.72rem;
  opacity: 0.9;
}

.s-agent-hero__chips {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.s-agent-hero__chips-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.55);
  text-align: left;
}

.s-agent-hero__chips-row {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.s-agent-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 12px 5px 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.95);
  font-family: inherit;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.15s ease;
}

.s-agent-chip__icon {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 7px;
  font-size: 10px;
  color: #5eead4;
  background: rgba(13, 148, 136, 0.28);
  transition: background 0.2s ease, color 0.2s ease;
}

.s-agent-chip__text {
  white-space: nowrap;
}

.s-agent-chip:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(153, 246, 228, 0.35);
  box-shadow: 0 4px 14px rgba(13, 148, 136, 0.2);
  transform: translateY(-1px);
}

.s-agent-chip:hover .s-agent-chip__icon {
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
}

.s-agent-chip:active {
  transform: translateY(0);
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.user-nav {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 0.92rem;
}

.user-nav a {
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
}

.nav-link-msg {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.chat-nav-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
}

.nav-btn {
  background: var(--zx-glass-strong);
  border: 1px solid var(--zx-glass-border);
  color: #fff;
  padding: 7px 14px;
  border-radius: var(--zx-radius-md);
  cursor: pointer;
  font-size: 0.88rem;
  font-family: inherit;
  transition: background 0.2s;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.24);
}

.chat-page h1 {
  color: #fff;
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.02em;
}

.chat-page h1 i {
  margin-right: 8px;
  opacity: 0.95;
}

.chat-page .subtitle {
  color: rgba(255, 255, 255, 0.78);
  font-size: 0.86rem;
  max-width: 720px;
  margin: 8px auto 0;
  line-height: 1.55;
}

.template-bar {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.template-bar-label {
  color: rgba(255, 255, 255, 0.72);
  font-size: 0.86rem;
}

.template-chip {
  background: var(--zx-glass-strong);
  border: 1px solid var(--zx-glass-border);
  color: #fff;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 0.84rem;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s ease, transform 0.15s ease;
}

.template-chip:hover {
  background: rgba(255, 255, 255, 0.28);
  transform: translateY(-1px);
}

.role-badge {
  display: inline-block;
  margin: 10px 0 0;
  padding: 4px 12px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  border-radius: 999px;
  background: var(--zx-glass-strong);
  border: 1px solid var(--zx-glass-border);
  color: #fff;
}

.nav-link-dash {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  border-radius: var(--zx-radius-md);
  background: var(--zx-glass-strong);
  border: 1px solid var(--zx-glass-border);
}

.app-content {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 16px;
}

.chat-container {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--zx-surface);
  border-radius: var(--zx-radius-lg);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow: var(--zx-shadow-card);
}

.chat-container--student {
  border: 1px solid rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.96);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.6) inset,
    0 12px 40px rgba(15, 23, 42, 0.14),
    0 4px 16px rgba(13, 148, 136, 0.08);
}

.chat-container--student .chat-messages {
  background: linear-gradient(180deg, #f8fafc 0%, #f0fdfa 48%, #ecfeff 100%);
}

.s-welcome-panel {
  position: relative;
  align-self: flex-start;
  width: min(580px, 100%);
  margin: 4px 0 12px;
  padding: 20px 20px 16px;
  text-align: left;
  border-radius: 20px;
  background:
    linear-gradient(
      145deg,
      rgba(255, 255, 255, 0.98) 0%,
      rgba(240, 253, 250, 0.96) 48%,
      rgba(236, 254, 255, 0.94) 100%
    );
  border: 1px solid rgba(13, 148, 136, 0.16);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.85) inset,
    0 4px 6px rgba(15, 23, 42, 0.02),
    0 12px 32px rgba(13, 148, 136, 0.1),
    0 24px 48px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  animation: sWelcomeIn 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes sWelcomeIn {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.s-welcome-panel__glow {
  pointer-events: none;
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 70% 55% at 100% 0%, rgba(20, 184, 166, 0.14) 0%, transparent 58%),
    radial-gradient(ellipse 50% 45% at 0% 100%, rgba(34, 211, 238, 0.1) 0%, transparent 52%);
}

.s-welcome-panel__shine {
  pointer-events: none;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.95) 35%,
    rgba(153, 246, 228, 0.65) 50%,
    rgba(255, 255, 255, 0.95) 65%,
    transparent
  );
}

.s-welcome-panel__head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.s-welcome-panel__brand {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  min-width: 0;
  flex: 1;
}

.s-welcome-panel__copy {
  min-width: 0;
  flex: 1;
}

.s-welcome-panel__icon {
  position: relative;
  width: 48px;
  height: 48px;
  margin: 0;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 14px;
  font-size: 20px;
  color: #fff;
  background: linear-gradient(145deg, #0f766e 0%, #0d9488 48%, #14b8a6 100%);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.25) inset,
    0 6px 18px rgba(13, 148, 136, 0.32);
}

.s-welcome-panel__icon-ring {
  position: absolute;
  inset: -4px;
  border-radius: 17px;
  border: 1px solid rgba(13, 148, 136, 0.22);
  opacity: 0.7;
}

.s-welcome-panel__eyebrow {
  display: inline-flex;
  align-items: center;
  margin-bottom: 6px;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0f766e;
  background: rgba(13, 148, 136, 0.1);
  border: 1px solid rgba(13, 148, 136, 0.16);
}

.s-welcome-panel__title {
  margin: 0 0 5px;
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.2;
  color: #0f172a;
}

.s-welcome-panel__desc {
  margin: 0;
  font-size: 0.8rem;
  line-height: 1.55;
  color: #64748b;
}

.s-welcome-panel__badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.68rem;
  font-weight: 600;
  color: #0f766e;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(13, 148, 136, 0.14);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.s-welcome-panel__badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.2);
  animation: sWelcomeLivePulse 2s ease-in-out infinite;
}

@keyframes sWelcomeLivePulse {
  0%, 100% {
    box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.2);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(52, 211, 153, 0.08);
    transform: scale(1.08);
  }
}

.s-welcome-panel__intro {
  position: relative;
  z-index: 1;
  margin: 14px 0 0;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 0.82rem;
  line-height: 1.65;
  color: #334155;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(13, 148, 136, 0.08);
  border-left: 3px solid #14b8a6;
}

.s-welcome-panel__grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.s-welcome-cap {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 11px;
  border-radius: 14px;
  text-align: left;
  font-family: inherit;
  cursor: pointer;
  color: inherit;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(13, 148, 136, 0.12);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  transition:
    background 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.15s ease;
  -webkit-tap-highlight-color: transparent;
}

.s-welcome-cap:hover {
  background: #fff;
  border-color: rgba(13, 148, 136, 0.26);
  box-shadow: 0 4px 14px rgba(13, 148, 136, 0.12);
  transform: translateY(-1px);
}

.s-welcome-cap:active {
  transform: translateY(0);
}

.s-welcome-cap__icon {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  font-size: 0.82rem;
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  box-shadow: 0 3px 10px rgba(13, 148, 136, 0.22);
}

.s-welcome-cap__body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.s-welcome-cap__body strong {
  font-size: 0.8rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.01em;
}

.s-welcome-cap__body small {
  font-size: 0.68rem;
  line-height: 1.35;
  color: #64748b;
}

.s-welcome-cap__arrow {
  flex-shrink: 0;
  font-size: 0.58rem;
  color: #94a3b8;
  opacity: 0.7;
  transition: transform 0.2s ease, color 0.2s ease;
}

.s-welcome-cap:hover .s-welcome-cap__arrow {
  color: #0d9488;
  transform: translateX(2px);
}

.s-welcome-panel__footer {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(13, 148, 136, 0.1);
}

.s-welcome-panel__footer span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.68rem;
  font-weight: 600;
  color: #64748b;
}

.s-welcome-panel__footer i {
  font-size: 0.62rem;
  color: #0d9488;
  opacity: 0.85;
}

.s-chat-mobile-bar {
  display: none;
}

.s-chat-compose {
  flex-shrink: 0;
}

.s-chat-compose .s-mobile-chips {
  display: none;
}

.chat-header {
  flex-shrink: 0;
  background: linear-gradient(90deg, var(--zx-teal-dark), var(--zx-teal) 55%, var(--zx-teal-light));
  color: #fff;
  padding: 13px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.chat-header i {
  opacity: 0.92;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  padding: 22px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--zx-surface-muted);
}

.message {
  max-width: min(680px, 88%);
  padding: 0;
  border-radius: var(--zx-radius-md);
  line-height: 1.6;
  position: relative;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-message {
  align-self: flex-end;
  background: linear-gradient(135deg, var(--zx-teal-dark), var(--zx-teal-light));
  color: #fff;
  padding: 12px 16px 14px;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 4px 16px rgba(13, 148, 136, 0.28);
}

.ai-message {
  align-self: flex-start;
  background: #fff;
  color: var(--zx-text);
  padding: 12px 16px 14px;
  border-radius: 16px 16px 16px 4px;
  border: 1px solid var(--zx-border);
  box-shadow: var(--zx-shadow-soft);
}

.ai-message .ai-text-prose {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.72;
  font-size: 0.94rem;
  letter-spacing: 0.01em;
  color: #334155;
}

.ai-message .assistant-plain-bubble {
  width: 100%;
}

.assistant-plain-bubble {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-insert-cta-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  padding: 14px 16px;
  margin-top: 6px;
  border-radius: 14px;
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.9), rgba(240, 253, 250, 0.75));
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.profile-insert-cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 18px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  cursor: pointer;
  font-size: 0.86rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #fff;
  background: linear-gradient(180deg, #0f766e 0%, #0d9488 100%);
  box-shadow: 0 6px 18px rgba(13, 148, 136, 0.25);
  transition: transform 0.18s, box-shadow 0.18s;
}

.profile-insert-cta-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(13, 148, 136, 0.3);
}

.profile-insert-cta-hint {
  font-size: 0.8rem;
  color: #94a3b8;
  line-height: 1.45;
}

.chat-plain-text {
  white-space: pre-wrap;
  word-break: break-word;
}

/* v-html 子节点不受 scoped 属性影响，用 :deep 命中 */
.ai-message .ai-study-rich :deep(.ar-gap) {
  height: 0.5rem;
}

.ai-message .ai-study-rich :deep(.ar-block) {
  margin: 0 0 0.4rem;
  line-height: 1.72;
  font-size: 0.96rem;
}

.ai-message .ai-study-rich :deep(.ar-h) {
  font-weight: 700;
  font-size: 1.03rem;
  color: #0f172a;
  margin-top: 0.55rem;
  padding-bottom: 0.25rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.4);
  letter-spacing: 0.02em;
}

.ai-message .ai-study-rich :deep(.ar-li) {
  padding: 0.2rem 0 0.2rem 0.65rem;
  margin-left: 0.2rem;
  border-left: 3px solid rgba(13, 148, 136, 0.45);
  color: #334155;
}

.ai-message .ai-study-rich :deep(.ar-li.ar-num) {
  border-left-color: rgba(99, 102, 241, 0.5);
}

.ai-message .ai-study-rich :deep(.ar-p) {
  color: #334155;
}

.ai-message .ai-study-rich :deep(.ar-strong) {
  color: #1e293b;
  font-weight: 650;
}

.function-call-message {
  align-self: flex-start;
  background: transparent;
  color: #334155;
  padding: 0;
}

.function-message {
  align-self: flex-start;
  background: transparent;
  color: #334155;
  padding: 0;
}

.message-header {
  font-size: 0.76rem;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 0.02em;
}

.ai-message .message-header {
  color: var(--zx-teal-dark);
}

.user-message .message-header {
  color: rgba(255, 255, 255, 0.92);
}

.message-header i {
  width: 22px;
  height: 22px;
  border-radius: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.68rem;
  flex-shrink: 0;
}

.ai-message .message-header i {
  background: linear-gradient(135deg, var(--zx-teal-dark), var(--zx-teal-light));
  color: #fff;
}

.user-message .message-header i {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.function-call-message:has(.cf-result),
.function-call-message:has(.classroom-result),
.function-call-message:has(.classroom-confirm-card),
.function-call-message:has(.classroom-doc-card) {
  background: transparent;
  border: none;
  padding-left: 0;
  max-width: 100%;
  color: #334155;
}

.function-call-message:has(.cf-result) .message-header,
.function-call-message:has(.classroom-result) .message-header,
.function-call-message:has(.classroom-confirm-card) .message-header,
.function-call-message:has(.classroom-doc-card) .message-header {
  display: none;
}

.function-call-message:not(:has(.cf-result)):not(:has(.classroom-result)) .message-header {
  display: inline-flex;
  width: fit-content;
  padding: 4px 10px 4px 6px;
  border-radius: 999px;
  color: var(--zx-teal-dark, #0f766e);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(13, 148, 136, 0.18);
  box-shadow: 0 1px 3px rgba(13, 148, 136, 0.08);
}

.function-call-message:not(:has(.cf-result)):not(:has(.classroom-result)) .message-header i {
  background: linear-gradient(135deg, var(--zx-teal-dark), var(--zx-teal-light));
  color: #fff;
}

.ai-message:has(.review-rhythm-form-wrap),
.ai-message:has(.review-rhythm-plan-wrap),
.ai-message:has(.learning-reminder-form-wrap),
.ai-message:has(.learning-email-reminder-form-wrap),
.ai-message:has(.user-profile-form-wrap),
.ai-message:has(.app-nav-jump-wrap),
.ai-message:has(.messages-summary-wrap),
.ai-message:has(.assignments-summary-wrap),
.ai-message:has(.teacher-submissions-summary-wrap) {
  background: transparent;
  border: none;
  padding: 0;
  max-width: min(540px, 92%);
}

.review-rhythm-form-wrap,
.review-rhythm-plan-wrap,
.learning-reminder-form-wrap,
.learning-email-reminder-form-wrap,
.user-profile-form-wrap,
.app-nav-jump-wrap,
.messages-summary-wrap,
.assignments-summary-wrap,
.teacher-submissions-summary-wrap {
  margin-top: 0;
  width: min(540px, 100%);
}

.app-nav-jump-lead {
  margin: 0 0 10px;
  font-size: 14px;
  line-height: 1.55;
  color: #334155;
}

/* 天气 / 出行锦囊卡片容器 */
.weather-result-wrap {
  margin-top: 10px;
  max-width: 720px;
}

.chat-input {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 14px 18px 18px;
  background: #fff;
  border-top: 1px solid var(--zx-border);
  gap: 8px;
}

.chat-input--floating {
  padding: 0 16px 14px;
  background: transparent;
  border-top: none;
}

.chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.chat-input--floating .chat-input-bar {
  padding: 8px 8px 8px 12px;
  background: #fff;
  border: 1px solid rgba(13, 148, 136, 0.14);
  border-radius: 18px;
  box-shadow:
    0 2px 12px rgba(15, 23, 42, 0.06),
    0 0 0 1px rgba(255, 255, 255, 0.8) inset;
}

.chat-input-hint {
  margin: 0;
  padding: 0 6px;
  font-size: 0.72rem;
  color: var(--zx-muted);
  text-align: center;
}

.chat-input textarea {
  flex: 1;
  padding: 13px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  resize: none;
  height: 52px;
  font-size: 0.94rem;
  font-family: inherit;
  line-height: 1.45;
  background: #f8fafc;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.chat-input--floating textarea {
  border: none;
  background: transparent;
  padding: 10px 4px 10px 0;
  height: 48px;
}

.chat-input--floating textarea:focus {
  box-shadow: none;
}

.chat-input--floating .chat-input-bar:focus-within {
  border-color: var(--zx-teal);
  box-shadow:
    0 0 0 3px rgba(13, 148, 136, 0.12),
    0 4px 16px rgba(13, 148, 136, 0.1);
}

.chat-input textarea:focus {
  outline: none;
  border-color: var(--zx-teal);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.14);
}

.send-button {
  background: linear-gradient(135deg, var(--zx-teal-dark), var(--zx-teal-light));
  color: #fff;
  border: none;
  border-radius: 14px;
  padding: 0 22px;
  min-width: 92px;
  height: 52px;
  font-size: 0.92rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 14px rgba(13, 148, 136, 0.3);
  flex-shrink: 0;
}

.chat-input--floating .send-button,
.chat-input--floating .stop-button {
  min-width: 44px;
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 12px;
}

.chat-input--floating .send-btn-label {
  display: none;
}

.send-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(13, 148, 136, 0.35);
}

.send-button:disabled {
  background: #cbd5e1;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

.stop-button {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 14px;
  padding: 0 20px;
  min-width: 88px;
  height: 52px;
  font-size: 0.9rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}

.stop-button:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid var(--zx-border);
  border-radius: 16px 16px 16px 4px;
  align-self: flex-start;
  color: var(--zx-muted);
  font-size: 0.88rem;
  box-shadow: var(--zx-shadow-soft);
}

.dot {
  width: 7px;
  height: 7px;
  background: var(--zx-teal);
  border-radius: 50%;
  animation: bounce 1.5s infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.route-map-container {
  margin-top: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.travel-reminder-container {
  margin-top: 10px;
}

/* 酒店推荐图片容器 */
.hotel-result-wrapper {
  margin-top: 10px;
}

.food-result-wrapper {
  margin-top: 10px;
}

/* 酒店预订结果样式 */
.hotel-book-result {
  margin-top: 10px;
}

.book-success {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  border: 1px solid #a7f3d0;
  border-radius: 12px;
  padding: 16px 20px;
  color: #065f46;
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.book-success > i {
  font-size: 1.8rem;
  margin-top: 2px;
  color: #059669;
}

.book-info {
  font-size: 0.92rem;
  line-height: 1.7;
}

.book-title {
  font-weight: 700;
  font-size: 1.05rem;
  margin-bottom: 6px;
}

.book-link {
  margin-top: 8px;
}

.book-link a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.book-link a:hover {
  text-decoration: underline;
}

@media (max-width: 767px) {
  html.chat-route .chat-page .chat-input-hint {
    display: none;
  }

  html.chat-route .chat-page {
    padding: 0;
    padding-bottom: var(--mobile-nav-pad, calc(72px + env(safe-area-inset-bottom, 0px)));
    background: #f1f5f9;
  }

  html.chat-route .chat-page .app-container {
    gap: 0;
    max-width: none;
    height: 100%;
  }

  html.chat-route .chat-page .app-content {
    flex: 1;
    min-height: 0;
  }

  html.chat-route .chat-page .s-agent-hero {
    display: none;
  }

  html.chat-route .chat-page .user-nav,
  html.chat-route .chat-page .s-agent-hero__nav {
    display: none;
  }

  html.chat-route .chat-page > header:not(.s-agent-hero) .subtitle,
  html.chat-route .chat-page > header:not(.s-agent-hero) .template-bar {
    display: none;
  }

  html.chat-route .chat-page .chat-container--student {
    border: none;
    border-radius: 0;
    box-shadow: none;
    background: #f1f5f9;
    height: 100%;
  }

  .s-chat-mobile-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-shrink: 0;
    padding: calc(10px + env(safe-area-inset-top, 0px)) 14px 10px;
    background: linear-gradient(135deg, #047857 0%, #0d9488 55%, #14b8a6 100%);
    color: #fff;
    box-shadow: 0 2px 12px rgba(13, 148, 136, 0.2);
  }

  .s-chat-mobile-bar__brand {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .s-chat-mobile-bar__icon {
    width: 32px;
    height: 32px;
    display: grid;
    place-items: center;
    border-radius: 9px;
    font-size: 0.9rem;
    background: rgba(255, 255, 255, 0.18);
  }

  .s-chat-mobile-bar__title {
    font-family: var(--font-display);
    font-size: 1rem;
    font-weight: 800;
    letter-spacing: -0.02em;
  }

  .s-chat-mobile-bar__live {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 3px 9px;
    border-radius: 999px;
    font-size: 0.68rem;
    font-weight: 600;
    color: #ecfdf5;
    background: rgba(255, 255, 255, 0.14);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }

  .s-chat-mobile-bar__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #6ee7b7;
    box-shadow: 0 0 0 2px rgba(110, 231, 183, 0.3);
  }

  html.chat-route .chat-page .chat-container--student .chat-messages {
    padding: 12px 14px 8px;
    background: #f1f5f9;
  }

  .s-welcome-panel {
    padding: 14px 14px 12px;
    margin: 0 0 8px;
    border-radius: 18px;
    box-shadow:
      0 0 0 1px rgba(255, 255, 255, 0.75) inset,
      0 8px 24px rgba(13, 148, 136, 0.1);
  }

  .s-welcome-panel__head {
    flex-direction: column;
    gap: 10px;
  }

  .s-welcome-panel__badge {
    align-self: flex-start;
  }

  .s-welcome-panel__icon {
    width: 42px;
    height: 42px;
    font-size: 17px;
    border-radius: 12px;
  }

  .s-welcome-panel__title {
    font-size: 1rem;
  }

  .s-welcome-panel__desc {
    font-size: 0.74rem;
    line-height: 1.5;
  }

  .s-welcome-panel__intro {
    margin-top: 12px;
    padding: 8px 10px;
    font-size: 0.76rem;
    line-height: 1.55;
  }

  .s-welcome-panel__grid {
    grid-template-columns: 1fr;
    gap: 6px;
    margin-top: 12px;
  }

  .s-welcome-cap {
    padding: 9px 10px;
    border-radius: 12px;
  }

  .s-welcome-cap__icon {
    width: 32px;
    height: 32px;
    font-size: 0.76rem;
  }

  .s-welcome-panel__footer {
    margin-top: 10px;
    padding-top: 10px;
    gap: 6px 10px;
  }

  .s-chat-compose {
    flex-shrink: 0;
    background: #fff;
    border-top: 1px solid rgba(15, 23, 42, 0.06);
    box-shadow: 0 -4px 20px rgba(15, 23, 42, 0.06);
    padding-bottom: 4px;
  }

  .s-chat-compose .s-mobile-chips {
    display: flex;
    flex-shrink: 0;
    gap: 7px;
    padding: 8px 12px 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    background: #fff;
  }

  .s-chat-compose .s-mobile-chips::-webkit-scrollbar {
    display: none;
  }

  .s-mobile-chip {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 11px 6px 7px;
    border: 1px solid rgba(13, 148, 136, 0.14);
    border-radius: 999px;
    background: #f8fafc;
    color: #0f766e;
    font-family: inherit;
    font-size: 0.72rem;
    font-weight: 600;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
  }

  .s-mobile-chip i {
    display: grid;
    place-items: center;
    width: 20px;
    height: 20px;
    border-radius: 6px;
    font-size: 0.58rem;
    color: #fff;
    background: linear-gradient(135deg, #0f766e, #14b8a6);
  }

  .s-mobile-chip:active {
    background: #ecfdf5;
  }

  .s-mobile-chips--teacher .s-mobile-chip {
    border-color: rgba(99, 102, 241, 0.16);
    background: #eef2ff;
    color: #4338ca;
  }

  .s-mobile-chips--teacher .s-mobile-chip i {
    background: linear-gradient(135deg, #4f46e5, #6366f1);
  }

  .chat-input--floating {
    padding: 8px 12px 10px;
    background: transparent;
    border-top: none;
  }

  .chat-input--floating .chat-input-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 4px 4px 14px;
    background: #f8fafc;
    border: 1px solid rgba(13, 148, 136, 0.14);
    border-radius: 22px;
    box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.03);
  }

  .chat-input--floating .chat-input-bar:focus-within {
    border-color: rgba(13, 148, 136, 0.35);
    background: #fff;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  .chat-input--floating textarea {
    min-height: 40px;
    height: 40px;
    max-height: 96px;
    padding: 10px 0;
    line-height: 1.35;
    font-size: 16px;
    border: none;
    background: transparent;
    resize: none;
  }

  .chat-input--floating .send-button,
  .chat-input--floating .stop-button {
    min-width: 40px;
    width: 40px;
    height: 40px;
    padding: 0;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .chat-input--floating .send-button:not(:disabled) {
    background: linear-gradient(145deg, #0f766e, #14b8a6);
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.28);
  }

  .chat-input--floating .send-button:disabled {
    background: #e2e8f0;
    color: #94a3b8;
    box-shadow: none;
  }

  .app-content {
    flex-direction: column;
    min-height: 0;
  }

  .message {
    max-width: 92%;
  }

  .route-map-container {
    width: 100% !important;
    height: 400px !important;
  }
}

.s-mobile-chips {
  display: none;
}
</style>