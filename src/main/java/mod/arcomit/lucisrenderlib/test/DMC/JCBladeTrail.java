package mod.arcomit.lucisrenderlib.test.DMC;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.test.pipeline.BloomPipeline;
import mod.arcomit.lucisrenderlib.test.type.BloomPartcleRenderType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import static net.minecraft.util.Mth.nextFloat;

/**
 * 次元斩刀光轨迹粒子（单四边形渲染）
 * 实现刀光从起点向终点延伸并逐渐消失的视觉效果
 */
public class JCBladeTrail extends SingleQuadParticle {
    protected float timeOffset = 0f;  // 时间偏移量（用于错开粒子动画）
    protected final double X, Y, Z;  // 粒子初始位置（世界坐标）

    public JCBladeTrail(ClientLevel level, double x, double y, double z,
                        double rx, double ry, double rz) {
        super(level, x, y, z, rx, ry, rz);
        System.out.println("JCBladeTrail");  // 调试输出（实际应移除）
        this.lifetime = 9;                    // 粒子生命周期（9游戏刻≈0.45秒）
        timeOffset = nextFloat(random, 0, 1); // 随机时间偏移（0~1刻）
        X = x;  // 记录初始位置
        Y = y;
        Z = z;

        // 设置速度向量（实际用于表示终点方向）
        this.xd = rx;
        this.yd = ry;
        this.zd = rz;

        // 设置粒子颜色（RGBA：淡蓝色）
//        rCol = 0.55f;    // 红色分量
//        gCol = 0.6902f;  // 绿色分量
//        bCol = 1;        // 蓝色分量
//        alpha = 0.8f;    // 透明度
        rCol = 1;    // 红色分量
        gCol = 0;  // 绿色分量
        bCol = 0;        // 蓝色分量
        alpha = 0.8f;    // 透明度
    }

    @Override
    public boolean shouldCull() {
        return false;  // 禁用视锥体剔除
    }

    // 以下UV坐标方法未使用（因自定义渲染）
    @Override protected float getU0() { return 0; }
    @Override protected float getU1() { return 0; }
    @Override protected float getV0() { return 0; }
    @Override protected float getV1() { return 0; }

    @Override
    public void tick() {
        // 生命周期更新（不处理物理逻辑）
        if (this.age++ > this.lifetime) {
            this.remove();
        }
    }

    private static final ResourceLocation TEXTURE = Lucisrenderlib.prefix("textures/particle/sparks.png");

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        texturemanager.getTexture(TEXTURE).setFilter(false, true);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // 计算动画时间（考虑时间偏移）
        float animatedTime = this.age + partialTicks;
        if (animatedTime < timeOffset || animatedTime > lifetime + timeOffset) return;

        // 计算动画进度t（0~1，前40%时间）
        float t = Math.min(1, animatedTime / this.lifetime * 2.5f);

        // 获取相机位置
        Vec3 cameraPos = camera.getPosition();
        float cameraX = (float)(this.X - cameraPos.x());
        float cameraY = (float)(this.Y - cameraPos.y());
        float cameraZ = (float)(this.Z - cameraPos.z());

        // === 构建刀光几何体 ===
        // 1. 创建基础方向向量
        Vector3f right = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f dir = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);

        // 2. 计算当前长度（基于动画进度）
        dir.mul(t);

        // 3. 计算垂直方向（刀光宽度方向）
        right.cross(dir);
        right.normalize();

        // 4. 计算宽度衰减因子（后60%时间逐渐变窄）
        float widthFactor = (float) Math.sqrt(Math.min(1, (lifetime - animatedTime) / lifetime * 2.5f));
        right.mul(0.015f * widthFactor);  // 基础宽度0.015

        // 5. 创建左右偏移向量
        Vector3f left = new Vector3f(right);
        left.mul(-1);

        // 6. 定义四边形四个顶点
        Vector3f[] vertices = new Vector3f[4];
        vertices[0] = new Vector3f(right);  // 右起点
        vertices[1] = new Vector3f(left);   // 左起点
        vertices[2] = new Vector3f(right);  // 右终点
        vertices[3] = new Vector3f(right);  // 右终点（复制，实际使用vertices[2]）

        // 7. 设置终点位置
        vertices[2].add(dir);
        vertices[3].add(dir);  // 实际未使用

        // 8. 转换为世界坐标（相对相机）
        for (int i = 0; i < 4; i++) {
            vertices[i].add(cameraX, cameraY, cameraZ);
        }

        // === 纹理坐标（硬编码）===
        float u0 = 0, u1 = 1;  // U轴坐标
        float v0 = 0, v1 = 1;  // V轴坐标

        // 使用全亮度（自发光效果）
        int light = LightTexture.FULL_BRIGHT;

        // === 构建四边形 ===
        // 顶点顺序：右上 -> 左上 -> 左下 -> 右下（逆时针）
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv(u1, v1).uv2(light).endVertex();

        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv(u1, v0).uv2(light).endVertex();

        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv(u0, v0).uv2(light).endVertex();

        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())  // 实际与vertices[2]相同
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv(u0, v1).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return BloomPartcleRenderType.INSTANCE;
    }

    // 粒子工厂（客户端专用）
    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet spriteSet) { /* 纹理集未使用 */ }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new JCBladeTrail(world, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}