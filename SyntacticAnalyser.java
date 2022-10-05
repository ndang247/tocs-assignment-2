import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntacticAnalyser {

	public static ParseTree parse(List<Token> tokens) throws SyntaxException {
		// Turn the List of Tokens into a ParseTree.

		// If the tokens is empty
		if (tokens.isEmpty()) {
			// Throw a SyntaxException
			throw new SyntaxException("No tokens to parse: Empty token list");
		}

		ParseTree tree = new ParseTree();
		Deque<Pair<Symbol, TreeNode>> stack = new ArrayDeque<Pair<Symbol, TreeNode>>();

		// Add in the root
		stack.add(new Pair<Symbol, TreeNode>(TreeNode.Label.prog, null));

		int i = 0;

		while (i < tokens.size()) {
			// System.out.println("Stack: " + stack.peek().fst());
			// System.out.println("Token: " + tokens.get(i).getType());

			if (stack.peek().fst().isVariable()) {
				// Rule 1: <<prog>> -> public class <<ID>> { public static void main ( String []
				// args ) { <<los>> } }
				if (stack.peek().fst() == TreeNode.Label.prog && tokens.get(i).getType() == Token.TokenType.PUBLIC) {
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
				// Rule 2: <<los>> -> <<stat>> <<los>>
				else if (stack.peek().fst() == TreeNode.Label.los && (tokens.get(i).getType() == Token.TokenType.ID ||
						tokens.get(i).getType() == Token.TokenType.SEMICOLON ||
						tokens.get(i).getType() == Token.TokenType.WHILE ||
						tokens.get(i).getType() == Token.TokenType.FOR ||
						tokens.get(i).getType() == Token.TokenType.IF ||
						tokens.get(i).getType() == Token.TokenType.PRINT ||
						tokens.get(i).getType() == Token.TokenType.TYPE)) {

					TreeNode curr = new TreeNode(TreeNode.Label.los, stack.peek().snd());
					stack.peek().snd().addChild(curr);

					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.stat, curr));
				}
				// Rule 2.01: <<los>> -> e
				else if (stack.peek().fst() == TreeNode.Label.los
						&& tokens.get(i).getType() == Token.TokenType.RBRACE) {
					TreeNode curr = new TreeNode(TreeNode.Label.los, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
				}
				// Rule 3: <<stat>> -> <<while>>
				else if (stack.peek().fst() == TreeNode.Label.stat
						&& tokens.get(i).getType() == Token.TokenType.WHILE) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst((new Pair<Symbol, TreeNode>(TreeNode.Label.whilestat, curr)));
				}
				// Rule 3: <<stat>> -> <<for>>
				else if (stack.peek().fst() == TreeNode.Label.stat
						&& tokens.get(i).getType() == Token.TokenType.FOR) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.forstat, curr));
				}
				// Rule 3: <<stat>> -> <<if>>
				else if (stack.peek().fst() == TreeNode.Label.stat && tokens.get(i).getType() == Token.TokenType.IF) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.ifstat, curr));
				}
				// Rule 3: <<stat>> -> <<assign>>
				else if (stack.peek().fst() == TreeNode.Label.stat && tokens.get(i).getType() == Token.TokenType.ID) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.assign, curr));
				}
				// Rule 3: <<stat>> -> <<decl>>
				else if (stack.peek().fst() == TreeNode.Label.stat
						&& tokens.get(i).getType() == Token.TokenType.TYPE) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.decl, curr));
				}
				// Rule 3: <<stat>> -> <<print>>
				else if (stack.peek().fst() == TreeNode.Label.stat
						&& tokens.get(i).getType() == Token.TokenType.PRINT) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.print, curr));
				}
				// Rule 3: <<stat>> -> ;
				else if (stack.peek().fst() == TreeNode.Label.stat
						&& tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
					TreeNode curr = new TreeNode(TreeNode.Label.stat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.SEMICOLON, curr));
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
				// Rule 6: <<for start>> -> <<decl>>
				else if (stack.peek().fst() == TreeNode.Label.forstart
						&& tokens.get(i).getType() == Token.TokenType.TYPE) {
					TreeNode curr = new TreeNode(TreeNode.Label.forstart, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.decl, curr));
				}
				// Rule 6: <<for start>> -> <<assign>>
				else if (stack.peek().fst() == TreeNode.Label.forstart
						&& tokens.get(i).getType() == Token.TokenType.ID) {
					TreeNode curr = new TreeNode(TreeNode.Label.forstart, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.assign, curr));
				}
				// Rule 6.01: <<for start>> -> e
				else if (stack.peek().fst() == TreeNode.Label.forstart
						&& tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
					TreeNode curr = new TreeNode(TreeNode.Label.forstart, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
				}
				// Rule 7: <<for arith>> -> <<arith expr>>
				else if (stack.peek().fst() == TreeNode.Label.forarith
						&& (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.NUM)) {
					TreeNode curr = new TreeNode(TreeNode.Label.forarith, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
				}
				// Rule 7.01: <<for arith>> -> e
				else if (stack.peek().fst() == TreeNode.Label.forarith
						&& tokens.get(i).getType() == Token.TokenType.RPAREN) {
					TreeNode curr = new TreeNode(TreeNode.Label.forarith, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
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
				// Rule 9: <<else if>> -> <<else?if>> { <<los>> } <<else if>>
				else if (stack.peek().fst() == TreeNode.Label.elseifstat
						&& tokens.get(i).getType() == Token.TokenType.ELSE) {
					TreeNode curr = new TreeNode(TreeNode.Label.elseifstat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.elseifstat, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RBRACE, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.los, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LBRACE, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.elseorelseif, curr));
				}
				// Rule 9.01: <<else if>> -> e
				else if (stack.peek().fst() == TreeNode.Label.elseifstat
						&& (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.RBRACE
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.WHILE
								|| tokens.get(i).getType() == Token.TokenType.FOR
								|| tokens.get(i).getType() == Token.TokenType.IF
								|| tokens.get(i).getType() == Token.TokenType.PRINT
								|| tokens.get(i).getType() == Token.TokenType.TYPE)) {
					TreeNode curr = new TreeNode(TreeNode.Label.elseifstat, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
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
				// Rule 11: <<poss if>> -> if ( <<rel expr>> <<bool expr>> )
				else if (stack.peek().fst() == TreeNode.Label.possif
						&& tokens.get(i).getType() == Token.TokenType.IF) {
					TreeNode curr = new TreeNode(TreeNode.Label.possif, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.IF, curr));
				}
				// Rule 11.01: <<poss if>> -> e
				else if (stack.peek().fst() == TreeNode.Label.possif
						&& tokens.get(i).getType() == Token.TokenType.LBRACE) {
					TreeNode curr = new TreeNode(TreeNode.Label.possif, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
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
				else if (stack.peek().fst() == TreeNode.Label.decl && tokens.get(i).getType() == Token.TokenType.TYPE) {
					TreeNode curr = new TreeNode(TreeNode.Label.decl, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.possassign, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ID, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.type, curr));
				}
				// Rule 14: <<poss assign>> -> = <<expr>>
				else if (stack.peek().fst() == TreeNode.Label.possassign
						&& tokens.get(i).getType() == Token.TokenType.ASSIGN) {
					TreeNode curr = new TreeNode(TreeNode.Label.possassign, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.expr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ASSIGN, curr));
				}
				// Rule 14.01: <<poss assign>> -> e
				else if (stack.peek().fst() == TreeNode.Label.possassign
						&& tokens.get(i).getType() == Token.TokenType.SEMICOLON) {
					TreeNode curr = new TreeNode(TreeNode.Label.possassign, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
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
				// Rule 16: <<type>> -> int
				else if (stack.peek().fst() == TreeNode.Label.type
						&& tokens.get(i).getValue().get().equals("int")) {
					TreeNode curr = new TreeNode(TreeNode.Label.type, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TYPE, curr));
				}
				// Rule 16: <<type>> -> boolean
				else if (stack.peek().fst() == TreeNode.Label.type
						&& tokens.get(i).getValue().get().equals("boolean")) {
					TreeNode curr = new TreeNode(TreeNode.Label.type, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TYPE, curr));
				}
				// Rule 16: <<type>> -> char
				else if (stack.peek().fst() == TreeNode.Label.type
						&& tokens.get(i).getValue().get().equals("char")) {
					TreeNode curr = new TreeNode(TreeNode.Label.type, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TYPE, curr));
				}
				// Rulr 17: <<expr>> -> <<rel expr>> <<bool expr>>
				else if (stack.peek().fst() == TreeNode.Label.expr && (tokens.get(i).getType() == Token.TokenType.ID
						|| tokens.get(i).getType() == Token.TokenType.LPAREN
						|| tokens.get(i).getType() == Token.TokenType.TRUE
						|| tokens.get(i).getType() == Token.TokenType.FALSE
						|| tokens.get(i).getType() == Token.TokenType.NUM)) {
					TreeNode curr = new TreeNode(TreeNode.Label.expr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
				}
				// Rule 17.01: <<expr>> -> <<char expr>>
				else if (stack.peek().fst() == TreeNode.Label.expr
						&& tokens.get(i).getType() == Token.TokenType.SQUOTE) {
					TreeNode curr = new TreeNode(TreeNode.Label.expr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.charexpr, curr));
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
				// Rule 19: <<bool expr>> -> <<bool op>> <<rel expr>> <<bool expr>>
				else if (stack.peek().fst() == TreeNode.Label.boolexpr
						&& (tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR)) {
					TreeNode curr = new TreeNode(TreeNode.Label.boolexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolop, curr));
				}
				// Rule 19.01: <<bool expr>> -> e
				else if (stack.peek().fst() == TreeNode.Label.boolexpr
						&& (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON)) {
					TreeNode curr = new TreeNode(TreeNode.Label.boolexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
				}
				// Rule 20: <<bool op>> -> <<bool eq>>
				else if (stack.peek().fst() == TreeNode.Label.boolop
						&& (tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL)) {
					TreeNode curr = new TreeNode(TreeNode.Label.boolop, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.booleq, curr));
				}
				// Rule 20: <<bool op>> -> <<bool log>>
				else if (stack.peek().fst() == TreeNode.Label.boolop
						&& (tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR)) {
					TreeNode curr = new TreeNode(TreeNode.Label.boolop, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boollog, curr));
				}
				// Rule 21: <<bool eq>> -> ==
				else if (stack.peek().fst() == TreeNode.Label.booleq
						&& tokens.get(i).getType() == Token.TokenType.EQUAL) {
					TreeNode curr = new TreeNode(TreeNode.Label.booleq, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.EQUAL, curr));
				}
				// Rule 21: <<bool eq>> -> !=
				else if (stack.peek().fst() == TreeNode.Label.booleq
						&& tokens.get(i).getType() == Token.TokenType.NEQUAL) {
					TreeNode curr = new TreeNode(TreeNode.Label.booleq, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.NEQUAL, curr));
				}
				// Rule 22: <<bool log>> -> &&
				else if (stack.peek().fst() == TreeNode.Label.boollog
						&& tokens.get(i).getType() == Token.TokenType.AND) {
					TreeNode curr = new TreeNode(TreeNode.Label.boollog, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.AND, curr));
				}
				// Rule 22: <<bool log>> -> ||
				else if (stack.peek().fst() == TreeNode.Label.boollog
						&& tokens.get(i).getType() == Token.TokenType.OR) {
					TreeNode curr = new TreeNode(TreeNode.Label.boollog, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.OR, curr));
				}
				// Rule 23: <<rel expr>> -> <<arith expr>> <<rel expr'>>
				else if (stack.peek().fst() == TreeNode.Label.relexpr
						&& (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.NUM)) {
					TreeNode curr = new TreeNode(TreeNode.Label.relexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexprprime, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
				}
				// Rule 23 <<rel expr>> -> true
				else if (stack.peek().fst() == TreeNode.Label.relexpr
						&& tokens.get(i).getType() == Token.TokenType.TRUE) {
					TreeNode curr = new TreeNode(TreeNode.Label.relexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TRUE, curr));
				}
				// Rule 23: <<rel expr>> -> false
				else if (stack.peek().fst() == TreeNode.Label.relexpr
						&& tokens.get(i).getType() == Token.TokenType.FALSE) {
					TreeNode curr = new TreeNode(TreeNode.Label.relexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.FALSE, curr));
				}
				// Rule 24: <<rel expr'>> -> <<rel op>> <<arith expr>>
				else if (stack.peek().fst() == TreeNode.Label.relexprprime
						&& (tokens.get(i).getType() == Token.TokenType.LT
								|| tokens.get(i).getType() == Token.TokenType.LE
								|| tokens.get(i).getType() == Token.TokenType.GT
								|| tokens.get(i).getType() == Token.TokenType.GE)) {
					TreeNode curr = new TreeNode(TreeNode.Label.relexprprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relop, curr));
				}
				// Rule 24.01: <<rel expr'>> -> e
				else if (stack.peek().fst() == TreeNode.Label.relexprprime
						&& (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR)) {
					TreeNode curr = new TreeNode(TreeNode.Label.relexprprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
				}
				// Rule 25: <<rel op>> -> <
				else if (stack.peek().fst() == TreeNode.Label.relop
						&& tokens.get(i).getType() == Token.TokenType.LT) {
					TreeNode curr = new TreeNode(TreeNode.Label.relop, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LT, curr));
				}
				// Rule 25: <<rel op>> -> <=
				else if (stack.peek().fst() == TreeNode.Label.relop
						&& tokens.get(i).getType() == Token.TokenType.LE) {
					TreeNode curr = new TreeNode(TreeNode.Label.relop, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LE, curr));
				}
				// Rule 25: <<rel op>> -> >
				else if (stack.peek().fst() == TreeNode.Label.relop
						&& tokens.get(i).getType() == Token.TokenType.GT) {
					TreeNode curr = new TreeNode(TreeNode.Label.relop, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.GT, curr));
				}
				// Rule 25: <<rel op>> -> >=
				else if (stack.peek().fst() == TreeNode.Label.relop
						&& tokens.get(i).getType() == Token.TokenType.GE) {
					TreeNode curr = new TreeNode(TreeNode.Label.relop, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.GE, curr));
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
				// Rule 27: <<arith expr'>> -> + <<term>> <<arith expr'>>
				else if (stack.peek().fst() == TreeNode.Label.arithexprprime
						&& tokens.get(i).getType() == Token.TokenType.PLUS) {
					TreeNode curr = new TreeNode(TreeNode.Label.arithexprprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexprprime, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.term, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.PLUS, curr));
				}
				// Rule 27: <<arith expr'>> -> - <<term>> <<arith expr'>>
				else if (stack.peek().fst() == TreeNode.Label.arithexprprime
						&& tokens.get(i).getType() == Token.TokenType.MINUS) {
					TreeNode curr = new TreeNode(TreeNode.Label.arithexprprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexprprime, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.term, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.MINUS, curr));
				}
				// Rule 27.01: <<arith expr'>> -> e
				else if (stack.peek().fst() == TreeNode.Label.arithexprprime
						&& (tokens.get(i).getType() == Token.TokenType.RPAREN
								|| tokens.get(i).getType() == Token.TokenType.SEMICOLON
								|| tokens.get(i).getType() == Token.TokenType.EQUAL
								|| tokens.get(i).getType() == Token.TokenType.NEQUAL
								|| tokens.get(i).getType() == Token.TokenType.AND
								|| tokens.get(i).getType() == Token.TokenType.OR
								|| tokens.get(i).getType() == Token.TokenType.LT
								|| tokens.get(i).getType() == Token.TokenType.LE
								|| tokens.get(i).getType() == Token.TokenType.GT
								|| tokens.get(i).getType() == Token.TokenType.GE)) {
					TreeNode curr = new TreeNode(TreeNode.Label.arithexprprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
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
				// Rule 29: <<term'>> -> * <<factor>> <<term'>>
				else if (stack.peek().fst() == TreeNode.Label.termprime
						&& tokens.get(i).getType() == Token.TokenType.TIMES) {
					TreeNode curr = new TreeNode(TreeNode.Label.termprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.TIMES, curr));
				}
				// Rule 29: <<term'>> -> / <<factor>> <<term'>>
				else if (stack.peek().fst() == TreeNode.Label.termprime
						&& tokens.get(i).getType() == Token.TokenType.DIVIDE) {
					TreeNode curr = new TreeNode(TreeNode.Label.termprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.DIVIDE, curr));
				}
				// Rule 29: <<term'>> -> % <<factor>> <<term'>>
				else if (stack.peek().fst() == TreeNode.Label.termprime
						&& tokens.get(i).getType() == Token.TokenType.MOD) {
					TreeNode curr = new TreeNode(TreeNode.Label.termprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.termprime, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.factor, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.MOD, curr));
				}
				// Rule 29.01: <<term'>> -> e
				else if (stack.peek().fst() == TreeNode.Label.termprime
						&& (tokens.get(i).getType() == Token.TokenType.RPAREN
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
								|| tokens.get(i).getType() == Token.TokenType.MINUS)) {
					TreeNode curr = new TreeNode(TreeNode.Label.termprime, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					TreeNode epsilon = new TreeNode(TreeNode.Label.epsilon, curr);
					curr.addChild(epsilon);
				}
				// Rule 30: <<factor>> -> ( <<arith expr>> )
				else if (stack.peek().fst() == TreeNode.Label.factor
						&& tokens.get(i).getType() == Token.TokenType.LPAREN) {
					TreeNode curr = new TreeNode(TreeNode.Label.factor, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.RPAREN, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.arithexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.LPAREN, curr));
				}
				// Rule 30: <<factor>> -> <<ID>>
				else if (stack.peek().fst() == TreeNode.Label.factor
						&& tokens.get(i).getType() == Token.TokenType.ID) {
					TreeNode curr = new TreeNode(TreeNode.Label.factor, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.ID, curr));
				}
				// Rule 30: <<factor>> -> <<num>>
				else if (stack.peek().fst() == TreeNode.Label.factor
						&& tokens.get(i).getType() == Token.TokenType.NUM) {
					TreeNode curr = new TreeNode(TreeNode.Label.factor, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.NUM, curr));
				}
				// Rule 31: <<print expr>> -> <<rel expr>> <<bool expr>>
				else if (stack.peek().fst() == TreeNode.Label.printexpr
						&& (tokens.get(i).getType() == Token.TokenType.ID
								|| tokens.get(i).getType() == Token.TokenType.LPAREN
								|| tokens.get(i).getType() == Token.TokenType.TRUE
								|| tokens.get(i).getType() == Token.TokenType.FALSE
								|| tokens.get(i).getType() == Token.TokenType.NUM)) {
					TreeNode curr = new TreeNode(TreeNode.Label.printexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.boolexpr, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(TreeNode.Label.relexpr, curr));
				}
				// Rule 31: <<print expr>> -> " string lit "
				else if (stack.peek().fst() == TreeNode.Label.printexpr
						&& tokens.get(i).getType() == Token.TokenType.DQUOTE) {
					TreeNode curr = new TreeNode(TreeNode.Label.printexpr, stack.peek().snd());
					stack.peek().snd().addChild(curr);
					stack.pop();

					// Right hand side rule
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.DQUOTE, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.STRINGLIT, curr));
					stack.addFirst(new Pair<Symbol, TreeNode>(Token.TokenType.DQUOTE, curr));
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
