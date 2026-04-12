package iuh.fit.constance;

import javafx.scene.Parent;
import javafx.scene.Scene;

public final class AppTheme {
    public static final String PRIMARY = "#151C35";

    public static final String BG_APP = "#e8edf7";
    public static final String BG_CONTENT = "#eef2fb";
    public static final String BG_CARD = "#ffffff";
    public static final String BG_PLACEHOLDER = "#f8faff";
    public static final String BG_PRIMARY_OVERLAY = "rgba(8, 27, 143, 0.24)";

    public static final String TEXT_ON_PRIMARY = "#ffffff";
    public static final String TEXT_SUBTLE_ON_PRIMARY = "#cfd8ff";
    public static final String TEXT_MUTED = "#5f6f95";
    public static final String TEXT_SECONDARY = "#8695ba";
    public static final String TEXT_TITLE = "#0a1f7a";
    public static final String TEXT_ACCENT = "#0b1f84";
    public static final String TEXT_INPUT = "#edf2ff";
    public static final String TEXT_PLACEHOLDER = "#6f7ea8";
    public static final String TEXT_WARNING = "#ffcc9c";

    public static final String BORDER_SOFT = "#c6cff8";
    public static final String BORDER_PLACEHOLDER = "#dbe4ff";
    public static final String BORDER_FOCUS = "#2a7dff";
    public static final String BORDER_INPUT = "#ffffff";


    public static final String SHADOW_STRONG = "rgba(6, 29, 107, 0.14)";
    public static final String SHADOW_SOFT = "rgba(6, 29, 107, 0.1)";
    public static final String OVERLAY_HOVER = "rgba(255, 255, 255, 0.18)";

    private AppTheme() {
    }

    public static void applyTo(Scene scene) {
        if (scene == null) {
            return;
        }

        Parent root = scene.getRoot();
        if (root == null) {
            return;
        }

        String style = String.join(" ",
                "-color-primary: " + PRIMARY + ";",
                "-color-bg-app: " + BG_APP + ";",
                "-color-bg-content: " + BG_CONTENT + ";",
                "-color-bg-card: " + BG_CARD + ";",
                "-color-bg-placeholder: " + BG_PLACEHOLDER + ";",
                "-color-bg-primary-overlay: " + BG_PRIMARY_OVERLAY + ";",
                "-color-text-on-primary: " + TEXT_ON_PRIMARY + ";",
                "-color-text-subtle-on-primary: " + TEXT_SUBTLE_ON_PRIMARY + ";",
                "-color-text-muted: " + TEXT_MUTED + ";",
                "-color-text-secondary: " + TEXT_SECONDARY + ";",
                "-color-text-title: " + TEXT_TITLE + ";",
                "-color-text-accent: " + TEXT_ACCENT + ";",
                "-color-text-input: " + TEXT_INPUT + ";",
                "-color-text-placeholder: " + TEXT_PLACEHOLDER + ";",
                "-color-text-warning: " + TEXT_WARNING + ";",
                "-color-border-soft: " + BORDER_SOFT + ";",
                "-color-border-placeholder: " + BORDER_PLACEHOLDER + ";",
                "-color-border-focus: " + BORDER_FOCUS + ";",
                "-color-shadow-strong: " + SHADOW_STRONG + ";",
                "-color-shadow-soft: " + SHADOW_SOFT + ";",
                "-color-overlay-hover: " + OVERLAY_HOVER + ";",
                "-color-border-input: " + BORDER_INPUT + ";"
        );

        root.setStyle(style);
    }
}
