/**
 * starfield.js
 * 动态星空 Canvas 背景动画
 * @param {string} canvasId - HTML Canvas 元素的 ID (例如: 'starfieldCanvas')
 * @param {number} [maxStars=1300] - 最大的星星数量
 */
export function initStarfield(canvasId, maxStars = 1300) {
    const starfield = {
        canvas: null,
        ctx: null,
        w: 0,
        h: 0,
        stars: [],
        count: 0,
        maxStars: maxStars,
        hue: 217, // 颜色主色调（偏蓝）
        animationFrameId: null, // 存储 requestAnimationFrame ID
        
        cacheCanvas: null,
        cacheCtx: null
    };

    function random(min, max) {
        if (arguments.length < 2) {
            max = min;
            min = 0;
        }
        if (min > max) {
            var hold = max;
            max = min;
            min = hold;
        }
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    function maxOrbit(x, y) {
        var max = Math.max(x, y),
            diameter = Math.round(Math.sqrt(max * max + max * max));
        return diameter / 2;
    }

    function Star() {
        this.orbitRadius = random(maxOrbit(starfield.w, starfield.h));
        this.radius = random(60, this.orbitRadius) / 8; 
        this.orbitX = starfield.w / 2;
        this.orbitY = starfield.h / 2;
        this.timePassed = random(0, starfield.maxStars);
        this.speed = random(this.orbitRadius) / 1000000; 
        this.alpha = random(2, 10) / 1000;

        starfield.count++;
        starfield.stars[starfield.count] = this;
    }

    function drawStar(star) {
        const { ctx, cacheCanvas } = starfield;
        var x = Math.sin(star.timePassed) * star.orbitRadius + star.orbitX,
            y = Math.cos(star.timePassed) * star.orbitRadius + star.orbitY,
            twinkle = random(10);

        if (twinkle === 1 && star.alpha > 0) {
            star.alpha -= 0.05;
        } else if (twinkle === 2 && star.alpha < 1) {
            star.alpha += 0.05;
        }

        ctx.globalAlpha = star.alpha;
        ctx.drawImage(cacheCanvas, x - star.radius / 2, y - star.radius / 2, star.radius, star.radius);
        star.timePassed += star.speed;
    }

    function starfieldAnimation() {
        const { ctx, w, h, stars, hue } = starfield;
        
        // 绘制半透明的深色背景，用于制造拖尾效果
        ctx.globalCompositeOperation = 'source-over';
        ctx.globalAlpha = 0.5; 
        ctx.fillStyle = 'hsla(' + hue + ', 64%, 6%, 2)'; 
        ctx.fillRect(0, 0, w, h);

        ctx.globalCompositeOperation = 'lighter';
        for (var i = 1, l = stars.length; i < l; i++) {
            drawStar(stars[i]);
        }

        starfield.animationFrameId = window.requestAnimationFrame(starfieldAnimation);
    }
    
    // 初始化和启动逻辑
    function initialize() {
        starfield.canvas = document.getElementById(canvasId);
        if (!starfield.canvas) {
            console.error(`Canvas element with ID '${canvasId}' not found.`);
            return;
        }
        starfield.ctx = starfield.canvas.getContext('2d');
        
        // 初始化尺寸
        starfield.w = starfield.canvas.width = window.innerWidth;
        starfield.h = starfield.canvas.height = window.innerHeight;
        
        // 初始化缓存 Canvas (单个星星的模板)
        starfield.cacheCanvas = document.createElement('canvas');
        starfield.cacheCtx = starfield.cacheCanvas.getContext('2d');
        starfield.cacheCanvas.width = 100;
        starfield.cacheCanvas.height = 100;
        var half = starfield.cacheCanvas.width / 2,
            gradient2 = starfield.cacheCtx.createRadialGradient(half, half, 0, half, half, half);
            
        gradient2.addColorStop(0.025, '#CCC');
        gradient2.addColorStop(0.1, 'hsl(' + starfield.hue + ', 61%, 33%)');
        gradient2.addColorStop(0.25, 'hsl(' + starfield.hue + ', 64%, 6%)');
        gradient2.addColorStop(1, 'transparent');

        starfield.cacheCtx.fillStyle = gradient2;
        starfield.cacheCtx.beginPath();
        starfield.cacheCtx.arc(half, half, half, 0, Math.PI * 2);
        starfield.cacheCtx.fill();
        
        // 初始化星星实例
        starfield.stars = [];
        starfield.count = 0;
        for (var i = 0; i < starfield.maxStars; i++) {
            new Star();
        }
        
        // 启动动画
        starfieldAnimation();
    }
    
    // 对外暴露的清理和 resize 逻辑
    function cleanup() {
        if (starfield.animationFrameId) {
            window.cancelAnimationFrame(starfield.animationFrameId);
            starfield.animationFrameId = null;
        }
    }

    function resize() {
         if (starfield.canvas) {
            starfield.w = starfield.canvas.width = window.innerWidth;
            starfield.h = starfield.canvas.height = window.innerHeight;
        }
    }
    
    initialize();

    // 返回清理和 resize 函数，供 Vue 组件调用
    return { cleanup, resize };
}