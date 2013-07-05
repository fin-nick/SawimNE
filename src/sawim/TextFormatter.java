package sawim;

import DrawControls.icons.Icon;
import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.util.Log;
import sawim.modules.Emotions;

/**
 * Created with IntelliJ IDEA.
 * User: Gerc
 * Date: 19.06.13
 * Time: 20:45
 * To change this template use File | Settings | File Templates.
 */
public class TextFormatter {
    static Emotions smiles = Emotions.instance;

    private static void detectEmotions(Context context,
                                       SpannableStringBuilder builder, int startPos, int endPos) {
        for (int index = startPos; index < endPos; ++index) {
            int smileIndex = smiles.getSmile(builder.toString(), index);
            if (-1 != smileIndex) {
                int length = smiles.getSmileText(smileIndex).length();
                Icon icon = smiles.getSmileIcon(smileIndex);
                Bitmap bitmap = Bitmap.createBitmap(icon.getImage().getBitmap(), icon.x, icon.y, icon.getWidth(), icon.getHeight());
                ImageSpan imageSpan = new ImageSpan(context, bitmap, ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, index, index + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index += length - 1;
            }
        }
    }

    public static Spannable getFormattedText(String text, Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Linkify.addLinks(builder, 1);
        detectEmotions(context, builder, 0, text.length());
        builder.setSpan(null, 0, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }
}