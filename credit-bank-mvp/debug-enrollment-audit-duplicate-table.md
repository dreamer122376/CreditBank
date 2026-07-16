# Debug Session: enrollment-audit-duplicate-table

## Status: [OPEN]

### Basic Information
- **Issue**: 报名审核界面显示两张表（业务流程表 + 报名审核表），报名审核表显示"No Data"，但数据库存在符合条件的数据
- **Environment**: Windows / Spring Boot / Vue3 + Element Plus
- **Reproduction**: 登录系统 → 进入审核管理 → 点击"报名审核"标签

### Falsifiable Hypotheses

**Hypothesis 1**: 前端模板条件渲染逻辑错误，导致`activeTab === 'enrollment'`时同时显示了业务流程表和报名审核表
- *Observation*: 界面显示两张表，说明`v-if`/`v-else`逻辑可能有问题

**Hypothesis 2**: API返回数据格式不匹配，后端返回的是`{code, data}`包装结构，但前端直接使用数组
- *Observation*: 报名审核表显示"No Data"，可能是数据解析失败

**Hypothesis 3**: 机构管理员权限过滤导致查询不到数据，后端`getPendingAuditEnrollments`方法的机构过滤逻辑有误
- *Observation*: 数据库有数据但前端显示空，可能是权限过滤问题

**Hypothesis 4**: 前端`enrollmentApps`变量初始化或赋值逻辑有误，数据未正确加载
- *Observation*: 报名审核表为空，可能是变量未正确赋值

**Hypothesis 5**: 后端返回的StudentProjectAuditDTO字段名与前端表格`prop`属性不匹配
- *Observation*: 即使有数据，字段名不匹配也会显示为空

### Instrumentation Points

1. **前端**: Applications.vue - loadData函数中添加console.log，记录enrollList数据和长度
2. **后端**: StudentProjectService - getPendingAuditEnrollments方法添加System.out.println，记录操作人角色、机构ID、项目数量、报名数量

### Evidence Collection

**Pre-Fix Logs**: 等待用户复现

### Fix Applied

**Fix 1**: 修复前端条件渲染逻辑
- 文件: Applications.vue
- 问题: 当activeTab === 'enrollment'时，`v-if="activeTab !== 'conversion'"`条件为真，导致业务流程表和报名审核表同时显示
- 修改: 将条件改为 `v-if="activeTab !== 'conversion' && activeTab !== 'enrollment'"`

### Post-Fix Verification

*(待修复后填写)*

### Cleanup

*(待用户确认后填写)*