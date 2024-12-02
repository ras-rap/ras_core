package ras.core.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates Discord-like embeds using ASCII box-drawing characters.
 * Can be used to generate formatted ASCII text displays with various sections.
 */
public class AsciiEmbed {
    /**
     * Represents a field or section in the ASCII embed.
     */
    private static class EmbedSection {
        String name;  // Can be null for text-only sections
        String value;
        boolean isNamedSection;

        EmbedSection(String name, String value, boolean isNamedSection) {
            this.name = name;
            this.value = value;
            this.isNamedSection = isNamedSection;
        }
    }

    private String title;
    private String description;
    private String color;
    private List<EmbedSection> sections;

    /**
     * Creates a new AsciiEmbed with optional title, description, and color style.
     * 
     * @param title Optional title for the embed
     * @param description Optional description for the embed
     * @param color Border style ("single" or "double")
     */
    public AsciiEmbed(String title, String description, String color) {
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.color = color != null ? color : "single";
        this.sections = new ArrayList<>();
    }

    /**
     * Convenient constructor with default color.
     */
    public AsciiEmbed(String title, String description) {
        this(title, description, "single");
    }

    /**
     * Convenient constructor with empty title and description.
     */
    public AsciiEmbed() {
        this("", "");
    }

    /**
     * Adds a named field to the embed.
     * 
     * @param name Field name
     * @param value Field value
     * @return The AsciiEmbed instance for method chaining
     */
    public AsciiEmbed addField(String name, String value) {
        sections.add(new EmbedSection(name, value, true));
        return this;
    }

    /**
     * Adds a text-only section to the embed.
     * 
     * @param text Text to add as a section
     * @return The AsciiEmbed instance for method chaining
     */
    public AsciiEmbed addSection(String text) {
        sections.add(new EmbedSection(null, text, false));
        return this;
    }

    /**
     * Returns border characters based on the color style.
     */
    private String[] getBorderChars() {
        if ("double".equals(color)) {
            return new String[]{"╔", "╗", "╚", "╝", "═", "║"};
        }
        return new String[]{"┌", "┐", "└", "┘", "─", "│"};
    }

    /**
     * Centers text within a given width.
     */
    private String centerText(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        
        int padding = Math.max(0, (width - text.length()) / 2);
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    /**
     * Generates the ASCII embed.
     * 
     * @return Formatted ASCII embed as a string
     */
    public String generate() {
        String[] borderChars = getBorderChars();
        String topLeft = borderChars[0], topRight = borderChars[1],
               bottomLeft = borderChars[2], bottomRight = borderChars[3],
               horizontal = borderChars[4], vertical = borderChars[5];

        // Calculate dynamic width
        List<String> contentLines = new ArrayList<>();
        contentLines.add(title);
        contentLines.add(description);
        
        for (EmbedSection section : sections) {
            if (section.isNamedSection) {
                contentLines.add(section.name + ": " + section.value);
            } else {
                contentLines.add(section.value);
            }
        }

        int maxWidth = contentLines.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0);

        // Add extra padding for vertical bars
        int boxWidth = maxWidth + 2;

        // Build the embed
        List<String> embed = new ArrayList<>();

        // Top border
        embed.add(topLeft + horizontal.repeat(boxWidth) + topRight);

        // Title (if exists)
        if (!title.isEmpty()) {
            embed.add(vertical + " " + centerText(title, maxWidth) + " " + vertical);
        }

        // Divider
        embed.add(vertical + " ".repeat(boxWidth) + vertical);

        // Description (if exists)
        if (!description.isEmpty()) {
            embed.add(vertical + " " + String.format("%-" + maxWidth + "s", description) + " " + vertical);
        }

        // Space below description
        embed.add(vertical + " ".repeat(boxWidth) + vertical);

        // Sections
        for (EmbedSection section : sections) {
            String line;
            if (section.isNamedSection) {
                line = section.name + ": " + section.value;
            } else {
                line = section.value;
            }
            embed.add(vertical + " " + String.format("%-" + maxWidth + "s", line) + " " + vertical);
        }

        // Bottom border
        embed.add(bottomLeft + horizontal.repeat(boxWidth) + bottomRight);

        return String.join("\n", embed);
    }
}