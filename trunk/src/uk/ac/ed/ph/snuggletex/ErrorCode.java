/* $Id:ErrorCode.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Enumerates the various types of client-induced errors that can arise when using SnuggleTeX.
 * 
 * <h2>Developer Notes</h2>
 * 
 * <ul>
 *   <li>See the <tt>error-messages_en.properties</tt> file for descriptions of each code.</li>
 *   <li>To make things easier to read, add a comment whenever raising an error that briefly
 *     summarises the error.</li>
 *   <li>
 *     I'm using a kind of naming convention for these, as mentioned
 *     in the error-messages_en.properties. Note that the 3rd character
 *     indicates the type of error, which in all cases so far is 'E' for Error.
 *   </li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
public enum ErrorCode {
    
    /* Tokenisation errors */
    TTEG00(),
    TTEG01(),
    TTEG02(),
    TTEG03(),
    TTEM00(),
    TTEM01(),
    TTEM02(),
    TTEM03(),
    TTEG04(),
    TTEV00(),
    TTEV01(),
    TTEC00(),
    TTEC01(),
    TTEC02(),
    TTEC03(),
    TTEC04(),
    TTEE00(),
    TTEE01(),
    TTEE02(),
    TTEE03(),
    TTEE04(),
    TTEE05(),
    TTEE06(),
    TTEUC0(),
    TTEUC1(),
    TTEUC2(),
    TTEUC3(),
    TTEUC4(),
    TTEUC5(),
    TTEUC6(),
    TTEUC7(),
    TTEUC8(),
    TTEUC9(),
    TTEUE0(),
    TTEUE1(),
    TTEUE2(),
    TTEUE3(),
    TTEUE4(),
    
    /* Fixing errors */
    TFEG00(),
    TFEL00(),
    TFEM00(),
    TFEM01(),
    TFEM02(),
    TFEM03(),
    TFEM04(),
    TFETB0(),
    
    /* DOM Building errors */
    TDEG00(),
    TDEX00(),
    TDEX01(),
    TDEX02(),
    TDEX03(),
    TDEX04(),
    TDEM00(),
    TDEM01(),
    TDEMA0(),
    TDEMA1(),
    TDEL00(),
    TDETA0(),
    TDETA1(),
    TDETA2(),
    TDETB0(),
    TDETB1(),
    TDETB2(),
    TDETB3(),
    TDEUN0(),
    TDEUN1(),
    TDEUN2(),
    
    ;
}
