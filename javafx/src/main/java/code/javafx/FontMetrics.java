package code.javafx;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Font;
import java.util.Objects;

public class FontMetrics {

    /** The font loader. */
    private final FontLoader fontLoader;
    /** The font metrics. */
    private final com.sun.javafx.tk.FontMetrics fontMetrics;
    /** The font that was used to construct these metrics. */
    private final Font font;

    /**
     * Constructor.
     * @param font the font that was used to construct these metrics
     */
    private FontMetrics(Font font) {
        this.fontLoader = Toolkit.getToolkit().getFontLoader();
        this.font = Objects.requireNonNull(font);
        this.fontMetrics = fontLoader.getFontMetrics(font);
    }

    /**
     * The distance from the baseline to the max character height.
     * This value is always positive
     * @return the max ascent
     */
    public final float getMaxAscent() {
        return fontMetrics.getMaxAscent();
    }

    /**
     * The distance from the baseline to the avg max character height.
     * this value is always positive
     * @return the ascent
     */
    public final float getAscent() {
        return fontMetrics.getAscent();
    }

    /**
     * The distance from the baseline to the top of the avg. lowercase letter.
     * @return the x height
     */
    public final float getXheight() {
        return fontMetrics.getXheight();
    }

    /**
     * The baseline is the imaginary line upon which letters without descenders
     * (for example, the lowercase letter "a") sits. In terms of the font
     * metrics, all other metrics are derived from this point. This point is
     * implicitly defined as zero.
     * @return the baseline
     */
    public final int getBaseline() {
        return fontMetrics.getBaseline();
    }

    /**
     * The distance from the baseline down to the lowest avg. descender.
     * This value is always positive
     * @return the descent
     */
    public final float getDescent() {
        return fontMetrics.getDescent();
    }

    /**
     * The distance from the baseline down to the absolute lowest descender.
     * this value is always positive
     * @return the max descent
     */
    public final float getMaxDescent() {
        return fontMetrics.getMaxDescent();
    }

    /**
     * The amount of space between lines of text in this font. This is the
     * amount of space between he maxDecent of one line and the maxAscent
     * of the next. This number is included in the lineHeight.
     * @return the leading
     */
    public final float getLeading() {
        return fontMetrics.getLeading();
    }

    /**
     * The maximum line height for a line of text in this font.
     * maxAscent + maxDescent + leading
     * @return the line height
     */
    public final float getLineHeight() {
        return fontMetrics.getLineHeight();
    }

    /**
     * The font that was used to construct these metrics.
     * @return the font
     */
    public final Font getFont() {
        return font;
    }

    /**
     * Computes the width of the char when rendered with the font represented
     * by this FontMetrics instance.
     * @param ch the char
     * @return the width of the char
     */
    public float getCharWidth(char ch) {
        return fontLoader.getCharWidth(ch, font);
    }

}
