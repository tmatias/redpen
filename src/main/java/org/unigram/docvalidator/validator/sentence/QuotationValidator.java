package org.unigram.docvalidator.validator.sentence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unigram.docvalidator.DefaultSymbols;
import org.unigram.docvalidator.store.Sentence;
import org.unigram.docvalidator.util.CharacterTable;
import org.unigram.docvalidator.util.Configuration;
import org.unigram.docvalidator.util.DVCharacter;
import org.unigram.docvalidator.util.DocumentValidatorException;
import org.unigram.docvalidator.util.ValidationError;
import org.unigram.docvalidator.validator.SentenceValidator;

/**
 * Validator to check quotation characters.
 */
public class QuotationValidator implements SentenceValidator {

  public QuotationValidator() {
    super();
    this.useAscii = false;
    this.period = DefaultSymbols.get("FULL_STOP").getValue().charAt(0);
    leftSingleQuotationMark =
        DefaultSymbols.get("LEFT_SINGLE_QUOTATION_MARK");
    rightSingleQuotationMark =
        DefaultSymbols.get("RIGHT_SINGLE_QUOTATION_MARK");
    leftDoubleQuotationMark =
        DefaultSymbols.get("LEFT_DOUBLE_QUOTATION_MARK");
    rightDoubleQuotationMark =
        DefaultSymbols.get("RIGHT_DOUBLE_QUOTATION_MARK");
    exceptionSuffixes = DEFAULT_EXCEPTION_SUFFIXES;
  }

  public QuotationValidator(boolean isUseAscii) {
    this();
    this.useAscii = isUseAscii;
    if (useAscii) {
      leftSingleQuotationMark =
          new DVCharacter("LEFT_SINGLE_QUOTATION_MARK", "'", "", true, false);
      rightSingleQuotationMark =
          new DVCharacter("RIGHT_SINGLE_QUOTATION_MARK", "'", "", false, true);
      leftDoubleQuotationMark =
          new DVCharacter("LEFT_DOUBLE_QUOTATION_MARK", "\"", "", true, false);
      rightDoubleQuotationMark =
          new DVCharacter("RIGHT_DOUBLE_QUOTATION_MARK", "\"", "", false, true);
    }
  }

  public QuotationValidator(boolean isUseAscii, Character fullStop) {
    this(isUseAscii);
    this.period = fullStop;
  }

  @Override
  public List<ValidationError> check(Sentence sentence) {
    List<ValidationError> errors = new ArrayList<ValidationError>();
    // check single quotation
    List<ValidationError> result = this.checkQuotation(sentence,
        leftSingleQuotationMark, rightSingleQuotationMark);
    if (result != null) {
      errors.addAll(result);
    }

    // check double quotation
    errors.addAll(this.checkQuotation(sentence,
        leftDoubleQuotationMark, rightDoubleQuotationMark));
    return errors;
  }

  @Override
  public boolean initialize(Configuration conf, CharacterTable charTable)
      throws DocumentValidatorException {
    if (charTable.isContainCharacter("FULL_STOP")) {
      this.period = charTable.getCharacter("FULL_STOP").getValue().charAt(0);
    }

    if (conf.getAttribute("use_ascii").equals("true")) {
      useAscii = true;
      leftSingleQuotationMark =
          new DVCharacter("LEFT_SINGLE_QUOTATION_MARK", "'", "", true, false);
      rightSingleQuotationMark =
          new DVCharacter("RIGHT_SINGLE_QUOTATION_MARK", "'", "", false, true);
      leftDoubleQuotationMark =
          new DVCharacter("LEFT_DOUBLE_QUOTATION_MARK", "\"", "", true, false);
      rightDoubleQuotationMark =
          new DVCharacter("RIGHT_DOUBLE_QUOTATION_MARK", "\"", "", false, true);
    } else {
      // single quotes
      if (charTable.isContainCharacter("LEFT_SINGLE_QUOTATION_MARK")) {
        leftSingleQuotationMark =
            charTable.getCharacter("LEFT_SINGLE_QUOTATION_MARK");
      }
      if (charTable.isContainCharacter("RIGHT_SINGLE_QUOTATION_MARK")) {
        rightSingleQuotationMark =
            charTable.getCharacter("RIGHT_SINGLE_QUOTATION_MARK");
      }

      // single quotes
      if (charTable.isContainCharacter("LEFT_DOUBLE_QUOTATION_MARK")) {
        leftSingleQuotationMark =
            charTable.getCharacter("LEFT_DOUBLE_QUOTATION_MARK");
      }
      if (charTable.isContainCharacter("RIGHT_DOUBLE_QUOTATION_MARK")) {
        rightSingleQuotationMark =
            charTable.getCharacter("RIGHT_DOUBLE_QUOTATION_MARK");
      }
    }
    return true;
  }

  private List<ValidationError> checkQuotation(Sentence sentence,
      DVCharacter leftQuotation,
      DVCharacter rightQuotation) {
    String sentenceString = sentence.content;
    List<ValidationError> errors = new ArrayList<ValidationError>();
    int leftPosition = 0;
    int rightPosition = 0;
    while (leftPosition >= 0 && rightPosition < sentenceString.length()) {
      leftPosition = this.getQuotePosition(sentenceString,
          leftQuotation.getValue(),
          rightPosition + 1);

      if (leftPosition < 0) {
        rightPosition  = this.getQuotePosition(sentenceString,
            rightQuotation.getValue(),
            rightPosition + 1);
      } else {
        rightPosition  = this.getQuotePosition(sentenceString,
            rightQuotation.getValue(),
            leftPosition + 1);
      }

      // check if left and right quote pair exists
      if (leftPosition >= 0 && rightPosition < 0) {
        errors.add(new ValidationError(sentence.position,
            "Right Quotation mark does not exist: "
            + String.valueOf(sentence.content.length())
            + " in line: " + sentenceString));
        break;
      }

      if (leftPosition < 0 && rightPosition >= 0) {
        errors.add(new ValidationError(sentence.position,
            "left Quotation mark does not exist: "
            + String.valueOf(sentence.content.length())
            + " in line: " + sentenceString));
        break;
      }

      // check inconsistent quotation marks
      int nextLeftPosition  = this.getQuotePosition(sentenceString,
          leftQuotation.getValue(),
          leftPosition + 1);

      int nextRightPosition  = this.getQuotePosition(sentenceString,
          leftQuotation.getValue(),
          leftPosition + 1);

      if (nextLeftPosition < rightPosition && nextLeftPosition > 0) {
        errors.add(new ValidationError(sentence.position,
            "Twice Right Quotation marks in succession: "
            + " in line: " + sentenceString));
      }

      if (nextRightPosition < leftPosition && nextRightPosition > 0) {
        errors.add(new ValidationError(sentence.position,
            "Twice Left Quotation marks in succession: "
            + " in line: " + sentenceString));
      }

      // check if quotes have white spaces
      if (leftPosition > 0 && leftQuotation.isNeedBeforeSpace()
          && (sentenceString.charAt(leftPosition - 1) != ' ')) {
        errors.add(new ValidationError(sentence.position,
            "Left quotation does not have space: "
            + " in line: " + sentenceString));
      }

      if (rightPosition > 0 && rightPosition < sentenceString.length() - 1
          && rightQuotation.isNeedAfterSpace()
          && (sentenceString.charAt(rightPosition + 1) != ' '
          && sentenceString.charAt(rightPosition + 1) != this.period)) {
        errors.add(new ValidationError(sentence.position,
            "Right quotation does not have space"
                + " in line: " + sentenceString));
      }
    }
    return errors;
  }

  private int getQuotePosition(String sentenceStr, String quote,
      int startPosition) {
    int quoteCandidatePosition = startPosition;
    boolean isFound = false;
    while (startPosition > -1) {
      quoteCandidatePosition = sentenceStr.indexOf(quote, startPosition);
      isFound = detectIsFound(sentenceStr, quoteCandidatePosition);
      if (isFound) {
        return quoteCandidatePosition;
      } else if (quoteCandidatePosition >= 0 && !isFound) { // exception case
        startPosition = quoteCandidatePosition + 1;
      } else {
        return -1;
      }
    }
    return quoteCandidatePosition;
  }

  private boolean detectIsFound(String sentenceStr, final int startPosition) {
    if (startPosition < 0) {
      return false;
    }

    for (Iterator<String> ex = exceptionSuffixes.iterator(); ex.hasNext();) {
      if (sentenceStr.startsWith(ex.next(), startPosition + 1)) {
        return false;
      }
    }
    return true;
  }

  private static List<String> DEFAULT_EXCEPTION_SUFFIXES;

  static {
    DEFAULT_EXCEPTION_SUFFIXES = new ArrayList<String>();
    DEFAULT_EXCEPTION_SUFFIXES.add("s"); // He's
    DEFAULT_EXCEPTION_SUFFIXES.add("m"); // I'm
  }

  private DVCharacter leftSingleQuotationMark;

  private DVCharacter rightSingleQuotationMark;

  private DVCharacter leftDoubleQuotationMark;

  private DVCharacter rightDoubleQuotationMark;

  private List<String> exceptionSuffixes;

  private boolean useAscii;

  private Character period;
}
