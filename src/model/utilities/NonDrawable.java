package model.utilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <h4>Description</h4>
 * <p/> This annotation just tells the random class generators to IGNORE this class. This is usually because the right constructor is not present.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-29
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NonDrawable {
}
