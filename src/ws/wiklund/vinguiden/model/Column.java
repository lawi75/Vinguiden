package ws.wiklund.vinguiden.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Only supporting String columns today
 * @author wiklar
 *
 */

@Retention( value=RetentionPolicy.RUNTIME )
public @interface Column {
	String name();
}
