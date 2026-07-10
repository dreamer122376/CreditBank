/**
 * 学分银行系统 - 共享脚本
 * 负责登录态管理、角色主页跳转、模拟数据
 */

// ========== 模拟用户数据（演示用，后续接通后端登录接口后替换）==========
const MOCK_USERS = [
    { username: 'admin',     password: '123456', realName: '超级管理员', userType: 'SYSTEM_ADMIN',      institutionId: null,   institutionName: '' },
    { username: 'instadmin', password: '123456', realName: '李机构',    userType: 'INSTITUTION_ADMIN', institutionId: 1,      institutionName: '北京开放大学' },
    { username: 'student',   password: '123456', realName: '张三',      userType: 'STUDENT',           institutionId: 1,      institutionName: '北京开放大学' },
    { username: 'expert',    password: '123456', realName: '王教授',    userType: 'EXPERT',            institutionId: null,   institutionName: '清华大学' }
];

// ========== 角色与主页映射 ==========
const ROLE_HOME = {
    SYSTEM_ADMIN:      'home-system-admin.html',
    INSTITUTION_ADMIN: 'home-institution-admin.html',
    STUDENT:           'home-student.html',
    EXPERT:            'home-expert.html'
};

const ROLE_NAME = {
    SYSTEM_ADMIN:      '系统管理员',
    INSTITUTION_ADMIN: '机构管理员',
    STUDENT:           '学生',
    EXPERT:            '专家'
};

// ========== 登录态管理（基于 localStorage，演示用）==========
const Auth = {
    /** 保存登录用户信息 */
    login(user) {
        localStorage.setItem('cb_user', JSON.stringify(user));
    },
    /** 获取当前登录用户 */
    current() {
        const s = localStorage.getItem('cb_user');
        return s ? JSON.parse(s) : null;
    },
    /** 退出登录 */
    logout() {
        localStorage.removeItem('cb_user');
        location.href = 'login.html';
    },
    /** 页面鉴权：未登录跳转到登录页 */
    requireLogin() {
        const user = this.current();
        if (!user) {
            location.href = 'login.html';
            return null;
        }
        return user;
    },
    /** 校验角色是否匹配，不匹配跳转其主页 */
    requireRole(...roles) {
        const user = this.requireLogin();
        if (user && roles.length && !roles.includes(user.userType)) {
            location.href = ROLE_HOME[user.userType];
            return null;
        }
        return user;
    }
};

// ========== 模拟登录校验 ==========
function mockLogin(username, password) {
    const u = MOCK_USERS.find(u => u.username === username && u.password === password);
    return u ? { ...u } : null;
}

// ========== 模拟注册（仅存入 localStorage，刷新后失效）==========
function mockRegister(form) {
    // 演示用：直接构造用户对象，不校验重复
    return {
        username: form.username,
        password: form.password,
        realName: form.realName,
        userType: form.userType,
        institutionId: form.institutionId ? Number(form.institutionId) : null,
        institutionName: form.institutionName || ''
    };
}

// ========== 工具函数 ==========
/** 跳转到功能占位页，带功能名参数 */
function goFunc(funcName, sub) {
    let url = 'func.html?name=' + encodeURIComponent(funcName);
    if (sub) url += '&sub=' + encodeURIComponent(sub);
    location.href = url;
}

/** 获取 URL 查询参数 */
function getQuery(key) {
    return new URLSearchParams(location.search).get(key);
}

/** 渲染顶栏用户区域（含头像下拉框） */
function renderTopbar(targetId, user) {
    document.getElementById(targetId).innerHTML = `
        <div class="user-info">
            <div class="name">${user.realName}</div>
            <div class="role">${ROLE_NAME[user.userType] || ''}${user.institutionName ? ' · ' + user.institutionName : ''}</div>
        </div>
        <div class="avatar-wrap">
            <div class="avatar">${(user.realName || '?').charAt(0)}</div>
            <div class="avatar-dropdown">
                <div class="dropdown-item" onclick="Auth.logout()">
                    <span class="di-icon">⏻</span> 退出登录
                </div>
            </div>
        </div>
    `;
}

/** 退出登录按钮绑定 */
function bindLogout(btnId) {
    const el = document.getElementById(btnId);
    if (el) el.onclick = () => Auth.logout();
}
