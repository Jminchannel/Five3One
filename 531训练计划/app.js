// 531训练法助手 - 核心应用逻辑
class FivethreeoneApp {
    constructor() {
        this.data = {
            oneRM: {
                bench: 80,
                squat: 100,
                deadlift: 120,
                press: 55
            },
            trainingMax: {},
            plates: {
                barbell: 20,
                available: [25, 20, 10, 5, 2.5, 1.25]
            },
            template: '5s',
            currentWeek: 1,
            currentDay: 1,
            workoutHistory: [],
            settings: {
                restTime: 180
            }
        };
        
        this.timer = {
            interval: null,
            timeLeft: 0,
            totalTime: 0,
            isRunning: false
        };
        
        this.templates = {
            '5s': {
                name: '5/5/5+',
                week1: [0.65, 0.75, 0.85],
                week2: [0.70, 0.80, 0.90],
                week3: [0.75, 0.85, 0.95],
                week4: [0.40, 0.50, 0.60],
                reps: [5, 5, 5]
            },
            '3s': {
                name: '3/3/3+',
                week1: [0.70, 0.80, 0.90],
                week2: [0.75, 0.85, 0.95],
                week3: [0.80, 0.90, 1.00],
                week4: [0.40, 0.50, 0.60],
                reps: [3, 3, 3]
            },
            '531': {
                name: '5/3/1+',
                week1: [0.75, 0.85, 0.95],
                week2: [0.80, 0.90, 1.00],
                week3: [0.85, 0.95, 1.05],
                week4: [0.40, 0.50, 0.60],
                reps: [5, 3, 1]
            }
        };
        
        this.liftOrder = ['bench', 'squat', 'press', 'deadlift'];
        this.liftNames = {
            bench: '卧推',
            squat: '深蹲',
            press: '站姿推举',
            deadlift: '硬拉'
        };
        this.liftIcons = {
            bench: 'fas fa-bed',
            squat: 'fas fa-arrow-down',
            press: 'fas fa-hands-pray',
            deadlift: 'fas fa-arrow-up'
        };
        
        this.init();
    }
    
    init() {
        this.loadData();
        this.calculateTrainingMax();
        this.renderPlatesConfig();
        
        // 检查是否已完成初始设置
        if (this.isSetupComplete()) {
            this.goToScreen('dashboard-screen');
            this.updateDashboard();
        }
    }
    
    // 数据持久化
    saveData() {
        localStorage.setItem('531app-data', JSON.stringify(this.data));
    }
    
    loadData() {
        const saved = localStorage.getItem('531app-data');
        if (saved) {
            this.data = { ...this.data, ...JSON.parse(saved) };
        }
    }
    
    // 检查设置是否完成
    isSetupComplete() {
        return this.data.oneRM.bench > 0 && this.data.template && this.data.plates.available.length > 0;
    }
    
    // 计算训练最大重量 (90% 1RM)
    calculateTrainingMax() {
        Object.keys(this.data.oneRM).forEach(lift => {
            this.data.trainingMax[lift] = Math.round(this.data.oneRM[lift] * 0.9 * 2) / 2; // 四舍五入到0.5kg
        });
    }
    
    // 界面导航
    goToScreen(screenId) {
        document.querySelectorAll('.screen').forEach(screen => {
            screen.classList.remove('active');
        });
        document.getElementById(screenId).classList.add('active');
    }
    
    // 保存1RM设置
    save1RM() {
        this.data.oneRM.bench = parseFloat(document.getElementById('bench-1rm').value) || 80;
        this.data.oneRM.squat = parseFloat(document.getElementById('squat-1rm').value) || 100;
        this.data.oneRM.deadlift = parseFloat(document.getElementById('deadlift-1rm').value) || 120;
        this.data.oneRM.press = parseFloat(document.getElementById('press-1rm').value) || 55;
        
        this.calculateTrainingMax();
        this.saveData();
        this.goToScreen('plates-screen');
    }
    
    // 渲染杠铃片配置
    renderPlatesConfig() {
        const platesList = document.getElementById('plates-list');
        if (!platesList) return;
        
        const plateConfigs = [
            { weight: 25, color: 'bg-red-500', size: 'w-8 h-8' },
            { weight: 20, color: 'bg-blue-500', size: 'w-7 h-7' },
            { weight: 15, color: 'bg-yellow-500', size: 'w-6 h-6' },
            { weight: 10, color: 'bg-green-500', size: 'w-5 h-5' },
            { weight: 5, color: 'bg-purple-500', size: 'w-4 h-4' },
            { weight: 2.5, color: 'bg-gray-500', size: 'w-3 h-3' },
            { weight: 1.25, color: 'bg-gray-400', size: 'w-2 h-2' }
        ];
        
        platesList.innerHTML = plateConfigs.map(plate => {
            const isEnabled = this.data.plates.available.includes(plate.weight);
            return `
                <div class="flex items-center justify-between p-3 ${isEnabled ? 'bg-green-50 border border-green-200' : 'bg-gray-50 border'} rounded-lg">
                    <div class="flex items-center">
                        <div class="${plate.size} ${plate.color} rounded-full mr-3"></div>
                        <span class="font-semibold ${isEnabled ? '' : 'text-gray-400'}">${plate.weight}kg</span>
                    </div>
                    <button onclick="app.togglePlate(${plate.weight})" class="flex items-center space-x-3">
                        <span class="text-sm ${isEnabled ? 'text-gray-600' : 'text-gray-400'}">
                            ${isEnabled ? '数量: 4' : '不可用'}
                        </span>
                        <i class="fas ${isEnabled ? 'fa-check-circle text-green-500' : 'fa-times-circle text-gray-400'}"></i>
                    </button>
                </div>
            `;
        }).join('');
    }
    
    // 切换杠铃片可用性
    togglePlate(weight) {
        const index = this.data.plates.available.indexOf(weight);
        if (index > -1) {
            this.data.plates.available.splice(index, 1);
        } else {
            this.data.plates.available.push(weight);
            this.data.plates.available.sort((a, b) => b - a);
        }
        this.renderPlatesConfig();
        this.saveData();
    }
    
    // 设置杠铃杆重量
    setBarbellWeight(weight) {
        this.data.plates.barbell = weight;
        document.querySelectorAll('.barbell-btn').forEach(btn => {
            btn.classList.remove('bg-blue-100', 'text-blue-600');
            btn.classList.add('bg-gray-100', 'text-gray-600');
        });
        event.target.classList.remove('bg-gray-100', 'text-gray-600');
        event.target.classList.add('bg-blue-100', 'text-blue-600');
        this.saveData();
    }
    
    // 选择训练模板
    selectTemplate(templateId) {
        this.data.template = templateId;
        
        // 更新UI
        document.querySelectorAll('.template-card').forEach(card => {
            card.classList.remove('border-blue-200');
            card.querySelector('.template-check').classList.add('hidden');
        });
        
        event.currentTarget.classList.add('border-blue-200');
        event.currentTarget.querySelector('.template-check').classList.remove('hidden');
        
        // 启用完成按钮
        const completeBtn = document.getElementById('complete-setup-btn');
        completeBtn.disabled = false;
        completeBtn.classList.remove('bg-gray-400', 'cursor-not-allowed');
        completeBtn.classList.add('bg-blue-600');
        
        this.saveData();
    }
    
    // 完成设置
    completeSetup() {
        this.goToScreen('dashboard-screen');
        this.updateDashboard();
    }
    
    // 更新主界面
    updateDashboard() {
        this.updateTodayWorkoutCard();
        this.updateWeekProgress();
        this.updateTMOverview();
        this.updateCurrentWeekDisplay();
    }
    
    // 更新今日训练卡片
    updateTodayWorkoutCard() {
        const card = document.getElementById('today-workout-card');
        if (!card) return;
        
        const currentLift = this.liftOrder[this.data.currentDay - 1];
        const liftName = this.liftNames[currentLift];
        const templateName = this.templates[this.data.template].name;
        
        card.innerHTML = `
            <div class="flex justify-between items-start mb-3">
                <div>
                    <h3 class="font-bold text-lg mb-1">今日训练</h3>
                    <p class="opacity-90">${liftName}日 - ${templateName}</p>
                </div>
                <div class="bg-white bg-opacity-20 px-3 py-1 rounded-full">
                    <span class="text-sm font-semibold">第${this.data.currentWeek}周</span>
                </div>
            </div>
            
            <div class="bg-white bg-opacity-10 rounded-lg p-3 mb-3">
                <div class="flex items-center justify-between">
                    <span>主项训练</span>
                    <span class="font-bold">${liftName} 3组</span>
                </div>
            </div>
            
            <button onclick="app.startWorkout()" class="w-full bg-white text-blue-600 py-3 rounded-lg font-bold">
                开始训练
            </button>
        `;
    }
    
    // 更新本周进度
    updateWeekProgress() {
        const progress = document.getElementById('week-progress');
        if (!progress) return;
        
        const progressHTML = this.liftOrder.map((lift, index) => {
            const day = index + 1;
            const isCompleted = day < this.data.currentDay;
            const isCurrent = day === this.data.currentDay;
            const liftName = this.liftNames[lift];
            
            let statusHTML, statusText;
            if (isCompleted) {
                statusHTML = `<div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
                    <i class="fas fa-check text-green-600 text-sm"></i>
                </div>`;
                statusText = '<span class="text-green-600 text-sm font-semibold">已完成</span>';
            } else if (isCurrent) {
                statusHTML = `<div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
                    <span class="text-blue-600 text-sm font-bold">${day}</span>
                </div>`;
                statusText = '<span class="text-blue-600 text-sm font-semibold">进行中</span>';
            } else {
                statusHTML = `<div class="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center mr-3">
                    <span class="text-gray-400 text-sm font-bold">${day}</span>
                </div>`;
                statusText = '<span class="text-gray-400 text-sm">未开始</span>';
            }
            
            const dayNames = ['周一', '周三', '周五', '周日'];
            
            return `
                <div class="flex items-center justify-between">
                    <div class="flex items-center">
                        ${statusHTML}
                        <span class="${isCurrent ? 'font-semibold' : isCompleted ? '' : 'text-gray-400'}">${dayNames[index]} - ${liftName}</span>
                    </div>
                    ${statusText}
                </div>
            `;
        }).join('');
        
        progress.innerHTML = progressHTML;
    }
    
    // 更新TM概览
    updateTMOverview() {
        const overview = document.getElementById('tm-overview');
        if (!overview) return;
        
        const colors = ['text-red-500', 'text-green-500', 'text-orange-500', 'text-purple-500'];
        const overviewHTML = Object.keys(this.data.trainingMax).map((lift, index) => {
            return `
                <div class="text-center">
                    <div class="text-2xl font-bold ${colors[index]}">${this.data.trainingMax[lift]}kg</div>
                    <div class="text-sm text-gray-600">${this.liftNames[lift]}</div>
                </div>
            `;
        }).join('');
        
        overview.innerHTML = overviewHTML;
    }
    
    // 更新当前周显示
    updateCurrentWeekDisplay() {
        const display = document.getElementById('current-week');
        if (display) {
            display.textContent = `第${this.data.currentWeek}周 • 第${this.data.currentDay}天`;
        }
    }
    
    // 开始训练
    startWorkout() {
        this.goToScreen('workout-screen');
        this.renderWorkoutDetails();
    }
    
    // 渲染训练详情
    renderWorkoutDetails() {
        const currentLift = this.liftOrder[this.data.currentDay - 1];
        const liftName = this.liftNames[currentLift];
        const tm = this.data.trainingMax[currentLift];
        const template = this.templates[this.data.template];
        
        document.getElementById('workout-title').textContent = `${liftName}训练`;
        
        const percentages = template[`week${this.data.currentWeek}`];
        const reps = template.reps;
        
        const workoutContent = document.getElementById('workout-content');
        workoutContent.innerHTML = `
            <!-- 训练信息头部 -->
            <div class="bg-gradient-to-r from-green-500 to-teal-600 rounded-xl p-4 mb-4 text-white">
                <div class="flex justify-between items-start mb-2">
                    <div>
                        <h3 class="font-bold text-xl">${liftName}</h3>
                        <p class="opacity-90">第${this.data.currentWeek}周 • ${template.name} 模式</p>
                    </div>
                    <div class="text-right">
                        <div class="text-sm opacity-90">TM</div>
                        <div class="text-xl font-bold">${tm}kg</div>
                    </div>
                </div>
                <div class="bg-white bg-opacity-20 rounded-lg p-2">
                    <div class="text-sm">今日目标：完成3组主项训练</div>
                </div>
            </div>
            
            <!-- 主项训练组 -->
            <div class="bg-white rounded-xl p-4 mb-4 shadow-sm">
                <h4 class="font-semibold mb-3 flex items-center">
                    <i class="${this.liftIcons[currentLift]} text-green-600 mr-2"></i>主项训练
                </h4>
                
                ${percentages.map((percentage, index) => {
                    const weight = Math.round(tm * percentage * 2) / 2;
                    const repCount = index === 2 ? `${reps[index]}+` : reps[index];
                    const isAmrap = index === 2;
                    
                    return `
                        <div class="border ${isAmrap ? 'border-2 border-blue-200 bg-blue-50' : ''} rounded-lg p-3 mb-3">
                            <div class="flex justify-between items-center mb-2">
                                <div>
                                    <span class="font-semibold">第${index + 1}组</span>
                                    ${isAmrap ? '<span class="text-xs bg-blue-200 text-blue-800 px-2 py-1 rounded-full ml-2">AMRAP</span>' : ''}
                                </div>
                                <div class="flex items-center space-x-2">
                                    <button onclick="app.showPlateCalculator(${weight})" class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                                        <i class="fas fa-weight-hanging text-blue-600 text-sm"></i>
                                    </button>
                                    <div class="w-6 h-6 border-2 border-gray-300 rounded-full workout-check" data-set="${index}"></div>
                                </div>
                            </div>
                            <div class="text-lg font-bold">${weight}kg × ${repCount}次</div>
                            <div class="text-sm text-gray-600">${Math.round(percentage * 100)}% TM${isAmrap ? ' • 尽力完成更多次数' : ''}</div>
                            ${isAmrap ? `
                                <div class="flex items-center space-x-2 mt-2">
                                    <span class="text-sm">实际完成:</span>
                                    <button onclick="app.changeAmrapReps(-1)" class="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
                                        <i class="fas fa-minus text-gray-600"></i>
                                    </button>
                                    <span id="amrap-count" class="w-8 text-center font-bold">${reps[index]}</span>
                                    <button onclick="app.changeAmrapReps(1)" class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                                        <i class="fas fa-plus text-blue-600"></i>
                                    </button>
                                    <span class="text-sm text-gray-600">次</span>
                                </div>
                            ` : ''}
                        </div>
                    `;
                }).join('')}
            </div>
            
            <!-- 操作按钮 -->
            <div class="space-y-3">
                <button onclick="app.goToScreen('timer-screen')" class="w-full bg-green-600 text-white py-4 rounded-xl font-bold text-lg">
                    开始计时器
                </button>
                <button onclick="app.completeWorkout()" class="w-full bg-blue-600 text-white py-4 rounded-xl font-bold text-lg">
                    完成训练
                </button>
            </div>
        `;
    }
    
    // 修改AMRAP次数
    changeAmrapReps(delta) {
        const amrapDisplay = document.getElementById('amrap-count');
        if (!amrapDisplay) return;
        
        let current = parseInt(amrapDisplay.textContent);
        current = Math.max(0, current + delta);
        amrapDisplay.textContent = current;
    }
    
    // 完成训练
    completeWorkout() {
        const currentLift = this.liftOrder[this.data.currentDay - 1];
        const amrapCount = parseInt(document.getElementById('amrap-count')?.textContent || '0');
        
        // 记录训练
        const workout = {
            date: new Date().toISOString().split('T')[0],
            week: this.data.currentWeek,
            day: this.data.currentDay,
            lift: currentLift,
            amrapReps: amrapCount,
            template: this.data.template
        };
        
        this.data.workoutHistory.push(workout);
        
        // 更新进度
        this.data.currentDay++;
        if (this.data.currentDay > 4) {
            this.data.currentDay = 1;
            this.data.currentWeek++;
            
            // 检查是否完成周期
            if (this.data.currentWeek > 4) {
                this.data.currentWeek = 1;
                this.increaseTM();
            }
        }
        
        this.saveData();
        this.goToScreen('dashboard-screen');
        this.updateDashboard();
        
        // 显示完成提示
        this.showNotification('训练完成！', '很好的训练，继续保持！');
    }
    
    // 增加TM (周期结束后)
    increaseTM() {
        // 上肢 +2.5kg, 下肢 +5kg
        this.data.trainingMax.bench += 2.5;
        this.data.trainingMax.press += 2.5;
        this.data.trainingMax.squat += 5;
        this.data.trainingMax.deadlift += 5;
        
        this.showNotification('周期完成！', 'TM已自动增加，开始新的周期！');
    }
    
    // 显示配重计算器
    showPlateCalculator(weight) {
        document.getElementById('target-weight').value = weight;
        this.goToScreen('calculator-screen');
        this.calculatePlates();
    }
    
    // 计算杠铃配重
    calculatePlates() {
        const targetWeight = parseFloat(document.getElementById('target-weight').value);
        const barbellWeight = this.data.plates.barbell;
        const availablePlates = [...this.data.plates.available].sort((a, b) => b - a);
        
        const plateWeight = (targetWeight - barbellWeight) / 2; // 每侧需要的重量
        
        if (plateWeight <= 0) {
            this.renderPlateSolution(targetWeight, [], 'invalid');
            return;
        }
        
        // 贪心算法计算最佳配重
        const solution = this.findBestPlateCombo(plateWeight, availablePlates);
        this.renderPlateSolution(targetWeight, solution.plates, solution.error);
    }
    
    // 贪心算法找最佳杠铃片组合
    findBestPlateCombo(targetWeight, availablePlates) {
        const bestSolution = { plates: [], error: Infinity };
        
        // 尝试不同的组合
        const tryCombo = (remaining, usedPlates, plateIndex) => {
            if (plateIndex >= availablePlates.length || remaining <= 0) {
                const error = Math.abs(remaining);
                if (error < bestSolution.error) {
                    bestSolution.plates = [...usedPlates];
                    bestSolution.error = error;
                }
                return;
            }
            
            const plate = availablePlates[plateIndex];
            const maxCount = Math.min(2, Math.floor(remaining / plate)); // 最多2片
            
            for (let count = 0; count <= maxCount; count++) {
                const newUsed = [...usedPlates];
                for (let i = 0; i < count; i++) {
                    newUsed.push(plate);
                }
                tryCombo(remaining - plate * count, newUsed, plateIndex + 1);
            }
        };
        
        tryCombo(targetWeight, [], 0);
        return bestSolution;
    }
    
    // 渲染配重方案
    renderPlateSolution(targetWeight, plates, error) {
        const solution = document.getElementById('plate-solution');
        if (!solution) return;
        
        const totalWeight = this.data.plates.barbell + plates.reduce((sum, p) => sum + p, 0) * 2;
        const actualError = totalWeight - targetWeight;
        
        if (error === 'invalid' || plates.length === 0) {
            solution.innerHTML = `
                <div class="text-center p-4">
                    <i class="fas fa-exclamation-triangle text-yellow-500 text-3xl mb-3"></i>
                    <h4 class="font-semibold mb-2">无法配重</h4>
                    <p class="text-gray-600">目标重量过小或杠铃片不足</p>
                </div>
            `;
            return;
        }
        
        const plateColors = {
            25: 'bg-red-500',
            20: 'bg-blue-500',
            15: 'bg-yellow-500',
            10: 'bg-green-500',
            5: 'bg-purple-500',
            2.5: 'bg-gray-500',
            1.25: 'bg-gray-400'
        };
        
        const plateSizes = {
            25: 'w-6 h-16',
            20: 'w-5 h-14',
            15: 'w-4 h-12',
            10: 'w-4 h-10',
            5: 'w-3 h-8',
            2.5: 'w-3 h-6',
            1.25: 'w-2 h-6'
        };
        
        // 统计每种片的数量
        const plateCount = {};
        plates.forEach(plate => {
            plateCount[plate] = (plateCount[plate] || 0) + 1;
        });
        
        solution.innerHTML = `
            <div class="flex justify-between items-center mb-3">
                <h4 class="font-semibold">推荐配重方案</h4>
                <span class="text-sm ${Math.abs(actualError) < 0.1 ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'} px-2 py-1 rounded-full">
                    ${Math.abs(actualError) < 0.1 ? '精确匹配' : '近似匹配'}
                </span>
            </div>
            
            <!-- 总重量显示 -->
            <div class="text-center mb-4">
                <div class="text-2xl font-bold text-green-600">${totalWeight}kg</div>
                <div class="text-sm text-gray-600">
                    ${actualError > 0 ? `比目标重${actualError}kg` : actualError < 0 ? `比目标轻${Math.abs(actualError)}kg` : '精确匹配'}
                </div>
            </div>
            
            <!-- 杠铃分解 -->
            <div class="bg-gray-50 rounded-lg p-3 mb-4">
                <div class="text-center mb-3">
                    <div class="text-lg font-semibold">杠铃杆: ${this.data.plates.barbell}kg</div>
                </div>
                <div class="text-center">
                    <div class="font-semibold mb-2">每侧配重: ${plates.reduce((sum, p) => sum + p, 0)}kg</div>
                    <div class="text-sm text-gray-600">左右两侧各需要</div>
                </div>
            </div>
            
            <!-- 可视化杠铃 -->
            <div class="mb-4">
                <h5 class="font-semibold mb-2 text-center">杠铃配重示意图</h5>
                <div class="flex items-center justify-center space-x-1">
                    <!-- 左侧杠铃片 -->
                    <div class="flex items-center space-x-1">
                        ${[...plates].reverse().map(plate => 
                            `<div class="${plateSizes[plate]} ${plateColors[plate]} rounded" title="${plate}kg"></div>`
                        ).join('')}
                    </div>
                    
                    <!-- 杠铃杆 -->
                    <div class="w-24 h-3 bg-gray-700 rounded-full mx-2"></div>
                    
                    <!-- 右侧杠铃片 -->
                    <div class="flex items-center space-x-1">
                        ${plates.map(plate => 
                            `<div class="${plateSizes[plate]} ${plateColors[plate]} rounded" title="${plate}kg"></div>`
                        ).join('')}
                    </div>
                </div>
            </div>
            
            <!-- 详细配重列表 -->
            <div class="border-t pt-3">
                <h5 class="font-semibold mb-2">详细配重 (每侧)</h5>
                <div class="space-y-2">
                    ${Object.entries(plateCount).map(([plate, count]) => `
                        <div class="flex justify-between items-center">
                            <div class="flex items-center">
                                <div class="w-4 h-4 ${plateColors[plate]} rounded-full mr-2"></div>
                                <span>${plate}kg × ${count}</span>
                            </div>
                            <span class="font-semibold">${plate * count}kg</span>
                        </div>
                    `).join('')}
                    <div class="border-t pt-2 mt-2">
                        <div class="flex justify-between items-center font-bold">
                            <span>每侧总重:</span>
                            <span>${plates.reduce((sum, p) => sum + p, 0)}kg</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    // 计时器功能
    setTimer(seconds) {
        this.timer.timeLeft = seconds;
        this.timer.totalTime = seconds;
        this.updateTimerDisplay();
        this.updateTimerProgress();
    }
    
    toggleTimer() {
        if (this.timer.isRunning) {
            this.pauseTimer();
        } else {
            this.startTimer();
        }
    }
    
    startTimer() {
        if (this.timer.timeLeft <= 0) {
            this.setTimer(180); // 默认3分钟
        }
        
        this.timer.isRunning = true;
        const toggleBtn = document.getElementById('timer-toggle');
        if (toggleBtn) {
            toggleBtn.innerHTML = '<i class="fas fa-pause mr-2"></i>暂停';
        }
        
        this.timer.interval = setInterval(() => {
            this.timer.timeLeft--;
            this.updateTimerDisplay();
            this.updateTimerProgress();
            
            if (this.timer.timeLeft <= 0) {
                this.timerComplete();
            }
        }, 1000);
    }
    
    pauseTimer() {
        this.timer.isRunning = false;
        clearInterval(this.timer.interval);
        
        const toggleBtn = document.getElementById('timer-toggle');
        if (toggleBtn) {
            toggleBtn.innerHTML = '<i class="fas fa-play mr-2"></i>开始';
        }
    }
    
    stopTimer() {
        this.pauseTimer();
        this.timer.timeLeft = 0;
        this.timer.totalTime = 0;
        this.updateTimerDisplay();
        this.updateTimerProgress();
    }
    
    timerComplete() {
        this.pauseTimer();
        this.showNotification('时间到！', '休息时间结束，可以开始下一组了');
        
        // 可以添加声音或振动提醒
        if ('vibrate' in navigator) {
            navigator.vibrate([200, 100, 200]);
        }
    }
    
    updateTimerDisplay() {
        const display = document.getElementById('timer-display');
        if (!display) return;
        
        const minutes = Math.floor(this.timer.timeLeft / 60);
        const seconds = this.timer.timeLeft % 60;
        display.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }
    
    updateTimerProgress() {
        const circle = document.getElementById('progress-circle');
        const percent = document.getElementById('progress-percent');
        
        if (!circle || !percent) return;
        
        const progress = this.timer.totalTime > 0 ? this.timer.timeLeft / this.timer.totalTime : 0;
        const circumference = 2 * Math.PI * 56; // r=56
        const offset = circumference - (progress * circumference);
        
        circle.style.strokeDashoffset = offset;
        percent.textContent = `${Math.round(progress * 100)}%`;
    }
    
    // 统计功能
    updateStats() {
        this.updateGeneralStats();
        this.updateProgressStats();
        this.updateRecentWorkouts();
    }
    
    updateGeneralStats() {
        const completedWorkouts = document.getElementById('completed-workouts');
        const completedCycles = document.getElementById('completed-cycles');
        const avgAmrap = document.getElementById('avg-amrap');
        
        if (completedWorkouts) {
            completedWorkouts.textContent = this.data.workoutHistory.length;
        }
        
        if (completedCycles) {
            const cycles = Math.floor(this.data.workoutHistory.length / 16); // 4周 × 4天
            completedCycles.textContent = cycles;
        }
        
        if (avgAmrap) {
            const amrapWorkouts = this.data.workoutHistory.filter(w => w.amrapReps > 0);
            const average = amrapWorkouts.length > 0 ? 
                (amrapWorkouts.reduce((sum, w) => sum + w.amrapReps, 0) / amrapWorkouts.length).toFixed(1) : '0';
            avgAmrap.textContent = average;
        }
    }
    
    updateProgressStats() {
        const container = document.getElementById('progress-stats');
        if (!container) return;
        
        const colors = ['text-red-500', 'text-green-500', 'text-orange-500', 'text-purple-500'];
        const progressHTML = Object.keys(this.data.oneRM).map((lift, index) => {
            const current = this.data.trainingMax[lift];
            const initial = this.data.oneRM[lift] * 0.9; // 初始TM
            const progress = ((current - initial) / initial * 100).toFixed(1);
            
            return `
                <div class="flex justify-between items-center">
                    <div class="flex items-center">
                        <div class="w-3 h-3 ${colors[index].replace('text-', 'bg-')} rounded-full mr-3"></div>
                        <span>${this.liftNames[lift]}</span>
                    </div>
                    <div class="text-right">
                        <div class="font-semibold">${initial}kg → ${current}kg</div>
                        <div class="text-sm text-green-600">+${progress}%</div>
                    </div>
                </div>
            `;
        }).join('');
        
        container.innerHTML = progressHTML;
    }
    
    updateRecentWorkouts() {
        const container = document.getElementById('recent-workouts');
        if (!container) return;
        
        const recent = this.data.workoutHistory.slice(-5).reverse();
        const workoutsHTML = recent.map(workout => {
            const date = new Date(workout.date).toLocaleDateString('zh-CN');
            const liftName = this.liftNames[workout.lift];
            
            return `
                <div class="flex justify-between items-center p-3 bg-green-50 rounded-lg border border-green-200">
                    <div>
                        <div class="font-semibold">${liftName}训练</div>
                        <div class="text-sm text-gray-600">${date} • 第${workout.week}周第${workout.day}天</div>
                    </div>
                    <div class="text-right">
                        <div class="font-semibold text-green-600">完成</div>
                        <div class="text-sm text-gray-600">AMRAP: ${workout.amrapReps}次</div>
                    </div>
                </div>
            `;
        }).join('');
        
        container.innerHTML = workoutsHTML || '<p class="text-gray-500 text-center">暂无训练记录</p>';
    }
    
    // 数据管理
    exportData() {
        const dataStr = JSON.stringify(this.data, null, 2);
        const dataBlob = new Blob([dataStr], {type: 'application/json'});
        const url = URL.createObjectURL(dataBlob);
        
        const link = document.createElement('a');
        link.href = url;
        link.download = `531-training-data-${new Date().toISOString().split('T')[0]}.json`;
        link.click();
        
        URL.revokeObjectURL(url);
        this.showNotification('导出成功', '训练数据已导出到下载文件夹');
    }
    
    resetData() {
        if (confirm('确定要重置所有数据吗？此操作不可恢复！')) {
            localStorage.removeItem('531app-data');
            location.reload();
        }
    }
    
    // 通知系统
    showNotification(title, message) {
        // 创建简单的通知显示
        const notification = document.createElement('div');
        notification.className = 'fixed top-4 left-1/2 transform -translate-x-1/2 bg-white rounded-lg shadow-lg p-4 z-50 max-w-sm w-full mx-4';
        notification.innerHTML = `
            <div class="flex items-start">
                <i class="fas fa-check-circle text-green-500 mr-3 mt-1"></i>
                <div>
                    <div class="font-semibold">${title}</div>
                    <div class="text-sm text-gray-600">${message}</div>
                </div>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }
}

// 全局应用实例
const app = new FivethreeoneApp();

// 全局函数 (供HTML调用)
function goToScreen(screenId) {
    app.goToScreen(screenId);
    
    // 特殊页面的初始化
    if (screenId === 'stats-screen') {
        app.updateStats();
    } else if (screenId === 'calculator-screen') {
        app.calculatePlates();
    } else if (screenId === 'timer-screen') {
        app.setTimer(180); // 默认3分钟
    } else if (screenId === 'settings-screen') {
        // 更新设置页面的显示
        const templateDisplay = document.getElementById('current-template');
        const userInfo = document.getElementById('user-info');
        
        if (templateDisplay) {
            templateDisplay.textContent = app.templates[app.data.template].name;
        }
        if (userInfo) {
            userInfo.textContent = `531训练法 • 第${app.data.currentWeek}周期`;
        }
    }
}

function save1RM() {
    app.save1RM();
}

function setBarbellWeight(weight) {
    app.setBarbellWeight(weight);
}

function selectTemplate(templateId) {
    app.selectTemplate(templateId);
}

function completeSetup() {
    app.completeSetup();
}

function startWorkout() {
    app.startWorkout();
}

function calculatePlates() {
    app.calculatePlates();
}

function setTimer(seconds) {
    app.setTimer(seconds);
}

function toggleTimer() {
    app.toggleTimer();
}

function stopTimer() {
    app.stopTimer();
}

function exportData() {
    app.exportData();
}

function resetData() {
    app.resetData();
}
