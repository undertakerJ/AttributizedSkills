package net.lumi_noble.attributizedskills.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.client.screen.button.ExtraActionButton;
import net.lumi_noble.attributizedskills.client.screen.button.SkillButtonV2;
import net.lumi_noble.attributizedskills.common.attributes.util.AttributeBonus;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.config.ASConfig;
import net.lumi_noble.attributizedskills.common.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillScreenV2 extends Screen {
    public static final ResourceLocation SCREEN_TEXTURE = new ResourceLocation(AttributizedSkills.MOD_ID, "textures/gui/skill_screen_v2.png");
    private static final MutableComponent MENU_NAME = Component.translatable("container.skills");

    public SkillScreenV2() {
        super(MENU_NAME);
    }

    @Override
    protected void init() {
        int left = (width + 83 - 162) / 2 ;
        int top = (height - 224) / 2;

        for (int i = 0; i < 6; i++) {
            int x = left + (i % 2) * 83;
            int y = top + (i / 2) * 36;
            this.addRenderableWidget(new SkillButtonV2(x, y, Skill.values()[i]));
        }
        int extraY = top + 36*3;
    this.addRenderableWidget(
        new ExtraActionButton(
            left + 83/2,
            extraY,
            83,
            32,
            Component.literal("Inventory"),
            () -> {
              if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance()
                    .setScreen(new InventoryScreen(Minecraft.getInstance().player));
              }
            }));
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        int skillWidth = 256;
        int skillHeight = 256;

        int x = (this.width - skillWidth) / 2;
        int y = (this.height - skillHeight) / 2;

        int textureOriginOffset = 83;

        long ticks = Minecraft.getInstance().level.getGameTime();

        int frameHeight = 256;
        int totalFrames = 16;

        int currentFrame = (int) (ticks / 2 % totalFrames);

        int textureY = currentFrame * frameHeight;

        int textureWidth = 256;
        int textureHeight = 256 * 16;

        SkillModel model = SkillModel.get();
        String levelText = Component.translatable("gui.total_level").getString() + model.getTotalLevel();

        poseStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, SCREEN_TEXTURE);
        super.renderBackground(poseStack);
        GuiComponent.blit(
                poseStack, x, y, 0, textureY, skillWidth, skillHeight, textureWidth, textureHeight);
        font.draw(poseStack, levelText, (x + textureOriginOffset + 3) , y + 6, 0x969696);
        poseStack.popPose();
        font.draw(poseStack,
                Component.translatable("ui.skills.limit", ASConfig.getMaxLevelTotal()).getString(),
                x, y - 10, 0x313131);

        font.draw(poseStack,
                Component.translatable("ui.skills.hold_shift").getString(),
                x, y - 20, 0x313131);

        SkillButtonV2.drawOutlinedText(poseStack, "Tears: " + model.getTearPoints(), x + 250 - font.width("Tears: " + model.getTearPoints()), y + 6, 0x21F8F6);
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, partialTicks);

       renderBonusList(pPoseStack);
    }

    private int bonusScrollOffset = 0;

    private void renderBonusList(PoseStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int screenWidth = this.width;
        int screenHeight = this.height;

        int x = (screenWidth - 256) / 2;
        int y = (screenHeight - 256) / 2;
        int startX = x + 5;
        int bonusAreaWidth = 74;
        int bonusAreaHeight = 240;
        int baseLineHeight = font.lineHeight + 3;
        float bonusTextScale = 0.90F;
        int scaledLineHeight = (int) (baseLineHeight * bonusTextScale);

        List<MutableComponent> allLines = new ArrayList<>();
        SkillModel model = SkillModel.get();

        for (Skill skill : Skill.values()) {
            String header = Component.translatable(skill.displayName).getString() + ":";
            allLines.add(Component.literal(header));

            Map<String, AttributeBonus> bonusMap = getBonusMapForSkill(skill);
            int skillLevel = model.getSkillLevel(skill);
            int index = 1;
            if (bonusMap != null && !bonusMap.isEmpty()) {
                for (Map.Entry<String, AttributeBonus> entry : bonusMap.entrySet()) {
                    String attrId = entry.getKey();
                    AttributeBonus bonusData = entry.getValue();

                    Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
                    if (attribute == null) continue;
                    double totalValue = 0;
                    double baseValue = 0;
                    if (minecraft.player != null) {
                        AttributeInstance instance = minecraft.player.getAttribute(attribute);
                        if (instance != null) {
                            baseValue = instance.getBaseValue();
                            totalValue = instance.getValue();
                        }
                    }
          if (skillLevel > 1) {
            double bonusValue;
            switch (bonusData.operation) {
              case ADDITION:
                bonusValue = skillLevel * bonusData.multiplier;
                break;
              case MULTIPLY_BASE:
                bonusValue = baseValue * (skillLevel * bonusData.multiplier);
                break;
              case MULTIPLY_TOTAL:
                bonusValue = totalValue * (skillLevel * bonusData.multiplier);
                break;
              default:
                bonusValue = (skillLevel - 1) * bonusData.multiplier;
                break;
            }

            if (Math.abs(bonusValue) < 1e-6) continue;

            String attrName =
                I18n.exists(attribute.getDescriptionId())
                    ? I18n.get(attribute.getDescriptionId())
                    : attrId;

            String bonusLine =
                index + ". " + String.format("%.2f", bonusValue) + " bonus " + attrName;
            allLines.add(Component.literal(bonusLine));
            index++;
          }
        }
      } else {
                allLines.add(Component.literal("No bonuses"));
            }
            allLines.add(Component.literal(""));
        }
        List<MutableComponent> flattenedLines = new ArrayList<>();
        for (MutableComponent line : allLines) {
            List<MutableComponent> split = split(line, font, bonusAreaWidth);
            flattenedLines.addAll(split);
        }

        int totalLines = flattenedLines.size();
        int maxVisibleLines = bonusAreaHeight / scaledLineHeight;
        bonusScrollOffset = Mth.clamp(bonusScrollOffset, 0, Math.max(0, totalLines - maxVisibleLines));

        int drawY = y + 6;
        for (int i = bonusScrollOffset; i < totalLines && i < bonusScrollOffset + maxVisibleLines; i++) {
            MutableComponent comp = flattenedLines.get(i);
            // Если строка является заголовком (например, заканчивается на ":") — отрисовываем без масштабирования
            if (comp.getString().endsWith(":")) {
                font.draw(stack, comp, startX, drawY, 0xFFFFFF);
            } else {
                stack.pushPose();
                stack.scale(bonusTextScale, bonusTextScale, bonusTextScale);
                SkillButtonV2.drawOutlinedText(stack, comp.getString(), (int)(startX / bonusTextScale), (int)(drawY / bonusTextScale), 0xAAAAAA);
                stack.popPose();
            }
            drawY += scaledLineHeight;
            if (drawY >= y + bonusAreaHeight) break;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            bonusScrollOffset = Math.max(bonusScrollOffset - 1, 0);
        } else if (delta < 0) {
            int totalLines = getTotalBonusLines();
            int scaledLineHeight = (int) ((font.lineHeight + 2) * 0.90F);
            int maxVisibleLines = 10 / scaledLineHeight;
            bonusScrollOffset = Math.min(bonusScrollOffset + 1, Math.max(0, totalLines - maxVisibleLines));
        }
        return true;
    }

    private int getTotalBonusLines() {
        List<MutableComponent> flattenedLines = new ArrayList<>();
        SkillModel model = SkillModel.get();
        for (Skill skill : Skill.values()) {
            flattenedLines.add(Component.literal(Component.translatable(skill.displayName).getString() + ":"));
            Map<String, AttributeBonus> bonusMap = getBonusMapForSkill(skill);
            if (bonusMap != null && !bonusMap.isEmpty()) {
                int index = 1;
                int skillLevel = model.getSkillLevel(skill);
                for (Map.Entry<String, AttributeBonus> entry : bonusMap.entrySet()) {
                    String attrId = entry.getKey();
                    AttributeBonus bonusData = entry.getValue();
                    // Проверяем наличие атрибута
                    Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attrId));
                    if (attribute == null) continue;

                    // Получаем базовое значение атрибута (если доступно)
                    double baseValue = 0;
                    double totalValue = 0;
                    if (Minecraft.getInstance().player != null) {
                        AttributeInstance instance = Minecraft.getInstance().player.getAttribute(attribute);
                        if (instance != null) {
                            baseValue = instance.getBaseValue();
                            totalValue = instance.getValue();
                        }
                    }

            double bonusValue;
            switch (bonusData.operation) {
              case ADDITION:
                bonusValue = skillLevel * bonusData.multiplier;
                break;
              case MULTIPLY_BASE:
                bonusValue = baseValue * (skillLevel * bonusData.multiplier);
                break;
              case MULTIPLY_TOTAL:
                bonusValue = totalValue * (skillLevel * bonusData.multiplier);
                break;
              default:
                bonusValue = skillLevel * bonusData.multiplier;
                break;
            }

            if (Math.abs(bonusValue) < 1e-6) continue;

            String bonusLine = index + ". " + String.format("%.2f", bonusValue) + attrId;
            List<MutableComponent> splitLines = split(Component.literal(bonusLine), font, 74);
            flattenedLines.addAll(splitLines);
            index++;

        }
            } else {
                flattenedLines.add(Component.literal("No bonuses"));
            }
            flattenedLines.add(Component.literal(""));
        }
        return flattenedLines.size();
    }

    public static List<MutableComponent> split(Component component, Font font, int maxWidth) {
        String[] words = component.getString().split(" ");
        if (words.length < 2) {
            return List.of(component.copy());
        }
        List<MutableComponent> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            String nextLine = currentLine + " " + words[i];
            if (font.width(nextLine) > maxWidth) {
                lines.add(Component.literal(currentLine.toString()).withStyle(component.getStyle()));
                currentLine = new StringBuilder(" " + words[i]);
            } else {
                currentLine.append(" ").append(words[i]);
            }
        }
        lines.add(Component.literal(currentLine.toString()).withStyle(component.getStyle()));
        return lines;
    }

    private Map<String, AttributeBonus> getBonusMapForSkill(Skill skill) {
        ASConfig.load();
        switch (skill) {
            case VITALITY:
                return ASConfig.vitalityAttributeMultipliers;
            case STRENGTH:
                return ASConfig.strengthAttributeMultipliers;
            case MIND:
                return ASConfig.mindAttributeMultipliers;
            case DEXTERITY:
                return ASConfig.dexterityAttributeMultipliers;
            case ENDURANCE:
                return ASConfig.enduranceAttributeMultipliers;
            case INTELLIGENCE:
                return ASConfig.intelligenceAttributeMultipliers;
            default:
                return null;
        }
    }

}
