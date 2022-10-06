import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntacticAnalyser {

	public static ParseTree parse(List<Token> tokens) throws SyntaxException {
		// Turn the List of Tokens into a ParseTree.
		ParseTree tree = new ParseTree();
		Deque<Pair<Symbol, TreeNode>> stack = new ArrayDeque<Pair<Symbol, TreeNode>>();
		int i = 0;

		if (tokens.isEmpty()) {
			throw new SyntaxException("No tokens to parse: Empty token list");
		} else {
			// Add in the root
			stack.add(new Pair<Symbol, TreeNode>(TreeNode.Label.prog, null));

			while (i < tokens.size()) {
				if (stack.peek().fst().isVariable()) {
					// Rule 1: <<prog>> -> public class <<ID>> { public static void main ( String []
					// args ) { <<los>> } }
					if (stack.peek().fst() == TreeNode.Label.prog
							&& tokens.get(i).getType() == Token.TokenType.PUBLIC) {
						TreeNode curr = new TreeNode(TreeNode.Label.prog, null);
						tree.setRoot(curr);
						stack.pop();

						// Right hand side rule
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.PUBLIC, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.CLASS, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.ID, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.PUBLIC, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.STATIC, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.VOID, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.MAIN, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.STRINGARR, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.ARGS, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
						stack.add(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
						stack.add(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
					}
					// Rule 2: <<los>> -> <<stat>> <<los>> | e
					else if (stack.peek().fst() == TreeNode.Label.los) {
						TreeNode curr = new TreeNode(TreeNode.Label.los, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<los>> -> <<stat>> <<los>>
						if (tokens.get(i).getType() == Token.TokenType.ID ||
								tokens.get(i).getType() == Token.TokenType.SEMICOLON ||
								tokens.get(i).getType() == Token.TokenType.WHILE ||
								tokens.get(i).getType() == Token.TokenType.FOR ||
								tokens.get(i).getType() == Token.TokenType.IF ||
								tokens.get(i).getType() == Token.TokenType.PRINT ||
								tokens.get(i).getType() == Token.TokenType.TYPE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.stat, curr));
						}
						// <<los>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.RBRACE) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 3: <<stat>> -> <<while>> | <<for>> | <<if>> | <<assign>> ; | <<decl>> ;
					// |
					// <<print>> ; | ;
					else if (stack.peek().fst() == TreeNode.Label.stat) {
						TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<stat>> -> <<assign>> ;
						if (tokens.get(i).getType() == Token.TokenType.ID) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.assign, curr));
						}
						// <<stat>> -> ;
						else if (tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
						}
						// <<stat>> -> <<while>>
						else if (tokens.get(i).getType() == Token.TokenType.WHILE) {
							// Right hand side rule
							stack.addFirst((new Pair<Symbol, TreeNode>(TreeNode.Label.whilestat, curr)));
						}
						// <<stat>> -> <<for>>
						else if (tokens.get(i).getType() == Token.TokenType.FOR) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.forstat, curr));
						}
						// <<stat>> -> <<if>>
						else if (tokens.get(i).getType() == Token.TokenType.IF) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.ifstat, curr));
						}
						// <<stat>> -> <<print>> ;
						else if (tokens.get(i).getType() == Token.TokenType.PRINT) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.print, curr));
						}
						// <<stat>> -> <<decl>> ;
						else if (tokens.get(i).getType() == Token.TokenType.TYPE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.decl, curr));
						}
					}
					// Rule 4: <<while>> -> while ( <<rel expr>> <<bool expr>> ) { <<los>> }
					else if (stack.peek().fst() == TreeNode.Label.whilestat
							&& tokens.get(i).getType() == Token.TokenType.WHILE) {
						TreeNode curr = new TreeNode(TreeNode.Label.whilestat, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.WHILE, curr));
					}

					// Rule 5: <<for>> -> for ( <<for start>>; <<rel expr>> <<bool expr>>; <<for
					// arith>> ) { <<los>> }
					else if (stack.peek().fst() == TreeNode.Label.forstat
							&& tokens.get(i).getType() == Token.TokenType.FOR) {
						TreeNode curr = new TreeNode(TreeNode.Label.forstat, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.forarith, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.forstart, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.FOR, curr));
					}
					// Rule 6: <<for start>> -> <<decl>> | <<assign>> | e
					else if (stack.peek().fst() == TreeNode.Label.forstart) {
						TreeNode curr = new TreeNode(TreeNode.Label.forstart, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<for start>> -> <<decl>>
						if (tokens.get(i).getType() == Token.TokenType.ID) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.decl, curr));
						}
						// <<for start>> -> <<assign>>
						else if (tokens.get(i).getType() == Token.TokenType.TYPE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.assign, curr));
						}
						// <<for start>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 7: <<for arith>> -> <<arith expr>> | e
					else if (stack.peek().fst() == TreeNode.Label.forarith) {
						TreeNode curr = new TreeNode(TreeNode.Label.forarith, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<for arith>> -> <<arith expr>>
						if (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.NUM) {

							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
						}
						// <<for arith>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.RPAREN) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 8: <<if>> -> if ( <<rel expr>> <<bool expr>> ) { <<los>> } <<else if>>
					else if (stack.peek().fst() == TreeNode.Label.ifstat
							&& tokens.get(i).getType() == Token.TokenType.IF) {
						TreeNode curr = new TreeNode(TreeNode.Label.ifstat, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.elseifstat, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.IF, curr));
					}
					// Rule 9: <<else if>> -> <<else?if>> { <<los>> } <<else if>> | e
					else if (stack.peek().fst() == TreeNode.Label.elseifstat) {
						TreeNode curr = new TreeNode(TreeNode.Label.elseifstat, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<else if>> -> <<else?if>> { <<los>> } <<else if>>
						if (tokens.get(i).getType() == Token.TokenType.ELSE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.elseifstat, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.elseorelseif, curr));
						}
						// <<else if>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.RBRACE
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.WHILE
								|| tokens.get(i).getType() == Token.TokenType.FOR
								|| tokens.get(i).getType() == Token.TokenType.IF
								|| tokens.get(i).getType() == Token.TokenType.PRINT
								|| tokens.get(i).getType() == Token.TokenType.TYPE) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 10: <<else?if>> -> else <<poss if>>
					else if (stack.peek().fst() == TreeNode.Label.elseorelseif
							&& tokens.get(i).getType() == Token.TokenType.ELSE) {
						TreeNode curr = new TreeNode(TreeNode.Label.elseorelseif, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.possif, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ELSE, curr));
					}
					// Rule 11: <<poss if>> -> if ( <<rel expr>> <<bool expr>> ) | e
					else if (stack.peek().fst() == TreeNode.Label.possif) {
						TreeNode curr = new TreeNode(TreeNode.Label.possif, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<poss if>> -> if ( <<rel expr>> <<bool expr>> )
						if (tokens.get(i).getType() == Token.TokenType.IF) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.IF, curr));
						}
						// <<poss if>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.LBRACE) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 12: <<assign>> -> <<ID>> = <<expr>>
					else if (stack.peek().fst() == TreeNode.Label.assign
							&& tokens.get(i).getType() == Token.TokenType.ID) {
						TreeNode curr = new TreeNode(TreeNode.Label.assign, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.expr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ASSIGN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ID, curr));
					}
					// Rule 13: <<decl>> -> <<type>> <<ID>> <<poss assign>>
					else if (stack.peek().fst() == TreeNode.Label.decl
							&& tokens.get(i).getType() == Token.TokenType.TYPE) {
						TreeNode curr = new TreeNode(TreeNode.Label.decl, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.possassign, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ID, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.type, curr));
					}
					// Rule 14: <<poss assign>> -> = <<expr>> | e
					else if (stack.peek().fst() == TreeNode.Label.possassign) {
						TreeNode curr = new TreeNode(TreeNode.Label.possassign, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<poss assign>> -> = <<expr>>
						if (tokens.get(i).getType() == Token.TokenType.ASSIGN) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.expr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ASSIGN, curr));
						}
						// <<poss assign>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 15: <<print>> -> System.out.println ( <<print expr>> )
					else if (stack.peek().fst() == TreeNode.Label.print
							&& tokens.get(i).getType() == Token.TokenType.PRINT) {
						TreeNode curr = new TreeNode(TreeNode.Label.print, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.printexpr, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.PRINT, curr));
					}
					// Rule 16: <<type>> -> int | boolean | char
					else if (stack.peek().fst() == TreeNode.Label.type && (tokens.get(i).getValue().get().equals("int")
							|| tokens.get(i).getValue().get().equals("boolean")
							|| tokens.get(i).getValue().get().equals("char"))) {
						TreeNode curr = new TreeNode(TreeNode.Label.type, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TYPE, curr));
					}
					// Rulr 17: <<expr>> -> <<rel expr>> <<bool expr>> | <<char expr>>
					else if (stack.peek().fst() == TreeNode.Label.expr) {
						TreeNode curr = new TreeNode(TreeNode.Label.expr, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<expr>> -> <<rel expr>> <<bool expr>>
						if (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.TRUE
								|| tokens.get(i).getType() == Token.TokenType.FALSE
								|| tokens.get(i).getType() == Token.TokenType.NUM) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
						}
						// <<expr>> -> <<char expr>>
						else if (tokens.get(i).getType() == Token.TokenType.SQUOTE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.charexpr, curr));
						}
					}
					// Rule 18: <<char expr>> -> ' <<char>> '
					else if (stack.peek().fst() == TreeNode.Label.charexpr
							&& tokens.get(i).getType() == Token.TokenType.SQUOTE) {
						TreeNode curr = new TreeNode(TreeNode.Label.charexpr, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SQUOTE, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.CHARLIT, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SQUOTE, curr));
					}
					// Rule 19: <<bool expr>> -> <<bool op>> <<rel expr>> <<bool expr>> | e
					else if (stack.peek().fst() == TreeNode.Label.boolexpr) {
						TreeNode curr = new TreeNode(TreeNode.Label.boolexpr, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<bool expr>> -> <<bool op>> <<rel expr>> <<bool expr>>
						if (tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolop, curr));
						}
						// <<bool expr>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 20: <<bool op>> -> <<bool eq>> | <<bool log>>
					else if (stack.peek().fst() == TreeNode.Label.boolop) {
						TreeNode curr = new TreeNode(TreeNode.Label.boolop, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<bool op>> -> <<bool eq>>
						if (tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.booleq, curr));
						}
						// <<bool op>> -> <<bool log>>
						else if (tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boollog, curr));
						}

					}
					// Rule 21: <<bool eq>> -> == | !=
					else if (stack.peek().fst() == TreeNode.Label.booleq) {
						TreeNode curr = new TreeNode(TreeNode.Label.booleq, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<bool eq>> -> ==
						if (tokens.get(i).getType() == Token.TokenType.EQUAL) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.EQUAL, curr));
						}
						// <<bool eq>> -> !=
						else if (tokens.get(i).getType() == Token.TokenType.NEQUAL) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.NEQUAL, curr));
						}
					}
					// Rule 22: <<bool log>> -> && | ||
					else if (stack.peek().fst() == TreeNode.Label.boollog) {
						TreeNode curr = new TreeNode(TreeNode.Label.boollog, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<bool log>> -> &&
						if (tokens.get(i).getType() == Token.TokenType.AND) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.AND, curr));
						}
						// <<bool log>> -> ||
						else if (tokens.get(i).getType() == Token.TokenType.OR) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.OR, curr));
						}
					}
					// Rule 23: <<rel expr>> -> <<arith expr>> <<rel expr'>> | true | false
					else if (stack.peek().fst() == TreeNode.Label.relexpr) {
						TreeNode curr = new TreeNode(TreeNode.Label.relexpr, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<rel expr>> -> <<arith expr>> <<rel expr'>>
						if (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.NUM) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexprprime, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
						}
						// <<rel expr>> -> true
						else if (tokens.get(i).getType() == Token.TokenType.TRUE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TRUE, curr));
						}
						// <<rel expr>> -> false
						else if (tokens.get(i).getType() == Token.TokenType.FALSE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.FALSE, curr));
						} else {
							throw new SyntaxException(
									"No rule found for " + stack.peek().fst() + " and " + tokens.get(i).getType());
						}
					}
					// Rule 24: <<rel expr'>> -> <<rel op>> <<arith expr>> | e
					else if (stack.peek().fst() == TreeNode.Label.relexprprime) {
						TreeNode curr = new TreeNode(TreeNode.Label.relexprprime, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<rel expr'>> -> <<rel op>> <<arith expr>>
						if (tokens.get(i).getType() == Token.TokenType.LT
								|| tokens.get(i).getType() == Token.TokenType.LE
								|| tokens.get(i).getType() == Token.TokenType.GT
								|| tokens.get(i).getType() == Token.TokenType.GE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relop, curr));
						}
						// <<rel expr'>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 25: <<rel op>> -> < | <= | > | >=
					else if (stack.peek().fst() == TreeNode.Label.relop) {
						TreeNode curr = new TreeNode(TreeNode.Label.relop, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<rel op>> -> <
						if (tokens.get(i).getType() == Token.TokenType.LT) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LT, curr));
						}
						// <<rel op>> -> <=
						else if (tokens.get(i).getType() == Token.TokenType.LE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LE, curr));
						}
						// <<rel op>> -> >
						else if (tokens.get(i).getType() == Token.TokenType.GT) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.GT, curr));
						}
						// <<rel op>> -> >=
						else if (tokens.get(i).getType() == Token.TokenType.GE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.GE, curr));
						}
					}
					// Rule 26: <<arith expr>> -> <<term>> <<arith expr'>>
					else if (stack.peek().fst() == TreeNode.Label.arithexpr
							&& (tokens.get(i).getType() == Token.TokenType.ID
									|| tokens.get(i).getType() == Token.TokenType.LPAREN
									|| tokens.get(i).getType() == Token.TokenType.NUM)) {
						TreeNode curr = new TreeNode(TreeNode.Label.arithexpr, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexprprime, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.term, curr));
					}
					// Rule 27: <<arith expr'>> -> + <<term>> <<arith expr'>> | - <<term>> <<arith
					// expr'>> | e
					else if (stack.peek().fst() == TreeNode.Label.arithexprprime) {
						TreeNode curr = new TreeNode(TreeNode.Label.arithexprprime, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<arith expr'>> -> + <<term>> <<arith expr'>>
						if (tokens.get(i).getType() == Token.TokenType.PLUS) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexprprime, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.term, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.PLUS, curr));
						}
						// <<arith expr'>> -> - <<term>> <<arith expr'>>
						else if (tokens.get(i).getType() == Token.TokenType.MINUS) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexprprime, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.term, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.MINUS, curr));
						}
						// <<arith expr'>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR
								|| tokens.get(i).getType() == Token.TokenType.LT
								|| tokens.get(i).getType() == Token.TokenType.LE
								|| tokens.get(i).getType() == Token.TokenType.GT
								|| tokens.get(i).getType() == Token.TokenType.GE) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 28: <<term>> -> <<factor>> <<term'>>
					else if (stack.peek().fst() == TreeNode.Label.term
							&& (tokens.get(i).getType() == Token.TokenType.ID
									|| tokens.get(i).getType() == Token.TokenType.LPAREN
									|| tokens.get(i).getType() == Token.TokenType.NUM)) {
						TreeNode curr = new TreeNode(TreeNode.Label.term, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// Right hand side rule
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
						stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
					}
					// Rule 29: <<term'>> -> * <<factor>> <<term'>> | / <<factor>> <<term'>> | %
					// <<factor>> <<term'>> | e
					else if (stack.peek().fst() == TreeNode.Label.termprime) {
						TreeNode curr = new TreeNode(TreeNode.Label.termprime, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<term'>> -> * <<factor>> <<term'>>
						if (tokens.get(i).getType() == Token.TokenType.TIMES) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TIMES, curr));
						}
						// <<term'>> -> / <<factor>> <<term'>>
						else if (tokens.get(i).getType() == Token.TokenType.DIVIDE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.DIVIDE, curr));
						}
						// <<term'>> -> % <<factor>> <<term'>>
						else if (tokens.get(i).getType() == Token.TokenType.MOD) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.MOD, curr));
						}
						// <<term'>> -> e
						else if (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR
								|| tokens.get(i).getType() == Token.TokenType.LT
								|| tokens.get(i).getType() == Token.TokenType.LE
								|| tokens.get(i).getType() == Token.TokenType.GT
								|| tokens.get(i).getType() == Token.TokenType.GE
								|| tokens.get(i).getType() == Token.TokenType.PLUS
								|| tokens.get(i).getType() == Token.TokenType.MINUS) {
							TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
							curr.addChild(epsilon);
						}
					}
					// Rule 30: <<factor>> -> ( <<arith expr>> ) | <<ID>> | <<num>>
					else if (stack.peek().fst() == TreeNode.Label.factor) {
						TreeNode curr = new TreeNode(TreeNode.Label.factor, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<factor>> -> ( <<arith expr>> )
						if (tokens.get(i).getType() == Token.TokenType.LPAREN) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
						}
						// <<factor>> -> <<ID>>
						else if (tokens.get(i).getType() == Token.TokenType.ID) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ID, curr));
						}
						// <<factor>> -> <<num>>
						else if (tokens.get(i).getType() == Token.TokenType.NUM) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.NUM, curr));
						} else {
							throw new SyntaxException(
									"No rule found for " + stack.peek().fst() + " and " + tokens.get(i).getType());
						}
					}
					// Rule 31: <<print expr>> -> <<rel expr>> <<bool expr>> | " string lit "
					else if (stack.peek().fst() == TreeNode.Label.printexpr) {
						TreeNode curr = new TreeNode(TreeNode.Label.printexpr, stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();

						// <<print expr>> -> <<rel expr>> <<bool expr>>
						if (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.TRUE
								|| tokens.get(i).getType() == Token.TokenType.FALSE
								|| tokens.get(i).getType() == Token.TokenType.NUM) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
						}
						// <<print expr>> -> " string lit "
						else if (tokens.get(i).getType() == Token.TokenType.DQUOTE) {
							// Right hand side rule
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.DQUOTE, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.STRINGLIT, curr));
							stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.DQUOTE, curr));
						}
					} else {
						throw new SyntaxException(
								"No rule found for " + stack.peek().fst() + " and " + tokens.get(i).getType());
					}
				} else if (!stack.peek().fst().isVariable()) {
					if (stack.peek().fst() == tokens.get(i).getType()) {
						// System.out.println("Pop: " + tokens.get(i).getType());
						TreeNode curr = new TreeNode(TreeNode.Label.terminal, tokens.get(i), stack.peek().snd());
						stack.peek().snd().addChild(curr);
						stack.pop();
						i++;
					} else {
						System.out.println("Error: " + stack.peek().fst() + " " + tokens.get(i).getType());
						throw new SyntaxException("Syntax Error");
					}
				}
			}
		}
		return tree;
	}
}

// The following class may be helpful.
class Pair<A, B> {
	private final A a;
	private final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A fst() {
		return a;
	}

	public B snd() {
		return b;
	}

	@Override
	public int hashCode() {
		return 3 * a.hashCode() + 7 * b.hashCode();
	}

	@Override
	public String toString() {
		return "{" + a + ", " + b + "}";
	}

	@Override
	public boolean equals(Object o) {
		if ((o instanceof Pair<?, ?>)) {
			Pair<?, ?> other = (Pair<?, ?>) o;
			return other.fst().equals(a) && other.snd().equals(b);
		}

		return false;
	}

}
