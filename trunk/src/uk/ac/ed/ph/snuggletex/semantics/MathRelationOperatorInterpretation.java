/* $Id: MathRelationOperatorInterpretation.java,v 1.5 2008/03/19 12:25:45 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * FIXME: This extends {@link MathOperatorInterpretation}, but has a different {@link InterpretationType}
 * which makes checking for operators a bit weird.
 * 
 * FIXME: This should maybe be called a "nottable" operator as that's the only difference it
 * has over its superclass.
 * 
 * @author  David McKain
 * @version $Revision: 1.5 $
 */
public class MathRelationOperatorInterpretation extends MathOperatorInterpretation {
    
    private final MathMLOperator notOperator;
    
    public MathRelationOperatorInterpretation(final MathMLOperator operator, final MathMLOperator notOperator) {
        super(operator);
        this.notOperator = notOperator;
    }
    
    public MathMLOperator getNotOperator() {
        return notOperator;
    }
    
    @Override
    public InterpretationType getType() {
        return InterpretationType.MATH_RELATION_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
