package mod.arcomit.lucisrenderlib.test.DMC;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.util.Mth.nextFloat;

/**
 * 完美次元斩刀痕粒子（核心功能是作为粒子发射器）
 * 继承自NoRenderParticle，自身不可见，专门用于生成刀光轨迹粒子
 */
public class JustJudgementCutMarksParticles extends NoRenderParticle {

    // 构造函数
    public JustJudgementCutMarksParticles(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        lifetime = 2500; // 设置粒子存在时间为2500游戏刻（约2分钟）
    }

    @Override
    public boolean shouldCull() {
        return false; // 禁用视锥体剔除，确保粒子始终更新
    }

    @Override
    public void tick() {
        // 生命周期检测
        if (this.age++ >= this.lifetime) {
            this.remove(); // 超过寿命时移除自身
            return;
        }

        // 每帧生成2个新粒子
        for (int i = 0; i < 2; i++) {
            /* === 生成起始点球坐标（相对位置） === */
            float r = nextFloat(random, 5, 8);          // 半径范围：5-8单位
            float theta = nextFloat(random, 0, 360);     // 水平角：0-360度
            float beta = nextFloat(random, 45, 80);      // 俯仰角：45-80度（避免垂直方向）

            /* === 生成终点球坐标（相对位置） === */
            float r2 = 13 - 5;                          // 固定半径：8单位
            // 终点角度在起点反方向基础上添加随机偏移
            float theta2 = nextFloat(random, 180 + theta - 45, 180 + theta + 45); // 水平角偏移：±45度
            float beta2 = nextFloat(random, 180 + beta - 20, 180 + beta + 20);    // 俯仰角偏移：±20度

            // 角度单位转换：度数 -> 弧度
            theta = (float) (theta / 180 * Math.PI);
            beta = (float) (beta / 180 * Math.PI);
            theta2 = (float) (theta2 / 180 * Math.PI);
            beta2 = (float) (beta2 / 180 * Math.PI);

            /* === 坐标转换：球坐标 -> 笛卡尔坐标 === */
            float scale = 1.35f; // 整体缩放因子

            // 计算起始点笛卡尔坐标 (sx, sy, sz)
            double sr = r * Math.sin(beta);
            double sx = sr * Math.sin(theta) * scale;
            double sy = r * Math.cos(beta) * scale;
            double sz = sr * Math.cos(theta) * scale;

            // 计算终点笛卡尔坐标 (ex, ey, ez)
            double er = r2 * Math.sin(beta2);
            double ex = er * Math.sin(theta2) * scale;  // 注意：终点使用相同缩放
            double ey = r2 * Math.cos(beta2) * scale;
            double ez = er * Math.cos(theta2) * scale;

            /* === 生成刀光轨迹粒子 === */
            Minecraft mc  = Minecraft.getInstance();
            mc.particleEngine.add(new JCBladeTrail(level,
                    sx + x,                // 世界坐标X = 起始点X + 发射器位置X
                    sy + y + 1.2,           // 世界坐标Y = 起始点Y + 发射器位置Y + 1.2（高度修正）
                    sz + z,                 // 世界坐标Z = 起始点Z + 发射器位置Z
                    (-ex - sx),             // 速度X分量：终点->起点的反向向量（制造拉伸效果）
                    ey - sy,                // 速度Y分量
                    (-ez - sz)              // 速度Z分量：终点->起点的反向向量
            ));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER; // 自身不可见
    }

    // 粒子工厂（客户端专用）
    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet; // 纹理集（虽未使用但需保留结构）

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            // 创建发射器粒子实例
            return new JustJudgementCutMarksParticles(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}