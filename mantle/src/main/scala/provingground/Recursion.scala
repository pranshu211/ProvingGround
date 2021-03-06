package provingground

import provingground.HoTT._
import provingground.Contexts._
//import Families._
import provingground.InductiveTypes._
//import provingground.Context.Context.empty

import scala.reflect.runtime.universe.{Try => UnivTry, Function => FunctionUniv, _}
import annotation._
import ConstructorPattern._

/**
  * Recursion and induction for inductive types.
  *
  * Recursion and induction are given in two ways:
  * * A context for each constructor, from which an identity is generated given an rhs: the kappaCtx maybe the better one for this.
  * * A formal recursion/induction function.
  *
  *
  */
object Recursion {
  /*
  /**
 * argument of the lhs of an equation corresponding to a specific constructor.
 *
 *  @param cnstr constructor
 *
 *  @param vars variable names to be applied to the constructor.
 */
  case class CnstrLHS(cnstr: Constructor[Term, Term], vars: List[AnySym]){
    /**
 * the argument of the lhs of identities for this constructor, which is a
 * term of type W obtained by applying the constructor to terms with given names.
 *
 */
    val arg = foldnames(cnstr.cons, vars)

    def varterms(W : Typ[Term]) = symbTerms(cnstr.pattern, vars, W)
    /**
 * Context for recursion
 * The type of this is also used for recursion
 */
    def recCtx[U <: Term](f: => Func[Term, U]) : Context[Term, Term] = {
      cnstrRecContext[Term](f, cnstr.pattern, vars,f.dom, f.codom)(Context.empty[Term])
    }

    /**
 * A formal object representing the type of the recursion function.
 */
    def recCtxVar(f: => Func[Term, Term]) : Term  = {
      recCtxTyp(f).symbObj(RecInduced(cnstr.cons, f))
    }

    /**
 * HoTT type of the recursion function
 */
    def recCtxTyp(f: => Func[Term, Term]) : Typ[Term]  = {
      recCtx(f)(f.codom)
    }

    def recCtxIdRHS(f: => Func[Term, Term]) = recCtx(f).foldinSym(f.codom)(recCtxVar(f))

    /**
 * context for inductive definitions.
 */

    def indCtx[U <: Term](f: => FuncLike[Term, U]) : Context[Term, Term] = {
      cnstrIndContext(f, cnstr.pattern, vars,f.dom, f.depcodom)(Context.empty[Term])


    }

    /**
 * HoTT type of the induction function
 */
    def indCtxTyp(f: => FuncLike[Term, Term]) : Typ[Term]  = {
      indCtx(f)(f.depcodom(arg))
    }

    def indCtxVar(f: => FuncLike[Term, Term]) : Term  = {
      indCtxTyp(f).symbObj(IndInduced(cnstr.cons, f))
    }

    def indCtxIdRHS(f: => FuncLike[Term, Term]) = indCtx(f).foldinSym(f.depcodom(arg))(indCtxVar(f))

    /**
 * change for building a kappa-context for recursion
 */
    private def kappaChange(f: => Func[Term, Term]) : Change[Term] = (varname, ptn, ctx) => {
    val x = ptn(f.dom).symbObj(varname)
    val fx = (ptn.induced(f))(x)
    x /: (fx |: ctx)
    	}

    /**
 * kappa-context for recursion, with f(n) etc. additional constants for parsing, but not variables.
 */
    def recKappaCtx(f: => Func[Term, Term]) : Context[Term, Term] = {
      cnstrContext[Term](cnstr.pattern, vars,f.dom, kappaChange(f))(Context.empty[Term])
    }

    /**
 * change for building a kappa-context for recursion
 */
    private def kappaIndChange(f: => FuncLike[Term, Term]) : Change[Term] = (varname, ptn, ctx) => {
    val x = ptn(f.dom).symbObj(varname)
    val fx = ptn.inducedDep(f)(x)
     x /: (fx |: ctx)
    	}

    /**
 * kappa-context for induction, with f(n) etc. additional constants for parsing, but not variables.
 */
    def indKappaCtx(f: => FuncLike[Term, Term]) : Context[Term, Term] = {
      cnstrContext[Term](cnstr.pattern, vars,f.dom, kappaIndChange(f))(Context.empty[Term])
    }

    /**
 * identity corresponding to the given constructor for a recursive definition.
 */
    def recIdentity(f: => Func[Term, Term])(rhs: Term) = DefnEqual(f(arg), rhs, varterms(f.dom))

    /**
 * identity corresponding to the given constructor for an inductive definition.
 */
    def indIdentity(f: => FuncLike[Term, Term])(rhs: Term) = DefnEqual(f(arg), rhs, varterms(f.dom))
  }

   /**
 * Change in context for a TypPattern (i.e., simple pattern ending in W).
 * Should allow for dependent types when making a lambda.
 */
  private def recContextChange[V<: Term with Subs[V]](f : => (Func[Term, Term]),
        W : Typ[Term], X : Typ[V]) : (AnySym, FmlyPtn[Term, Term], Context[Term, V]) => Context[Term, V] = (varname, ptn, ctx) => {
    val x = ptn(W).symbObj(varname)
    val fx = ptn.induced(f)(x)
    x /: fx /: ctx
  }


    /**
 * Change in context  for Induction for a TypPattern
 */
  private def indContextChange[V<: Term with Subs[V]](f : => (FuncLike[Term, Term]),
      W : Typ[V], Xs : Term => Typ[V]) : (AnySym, FmlyPtn[Term, Term], Context[Term, V]) => Context[Term, V] = (varname, ptn, ctx) =>   {
    val x =  ptn(W).symbObj(varname)
    val fx = ptn.inducedDep(f)(x)
    x /: fx /: ctx
  }

  /**
 * change in contexts given a type-pattern and a variable name.
 */
  private type Change[V <: Term with Subs[V]] = (AnySym, FmlyPtn[Term, Term]{type Cod = Term}, Context[Term, V]) => Context[Term, V]

  /**
 * Returns the context for a poly-pattern given change in context for a type-pattern.
 * Recursively defined as a change, applied  by default to an empty context.
 * Note that as both contexts and Poly-patterns are built right to left, it is natural to get consistency.
 *
 * @param ctx accumulator for context.
 *
 *
 */
  def cnstrContext[V<: Term with Subs[V]](
      ptn : ConstructorPattern[Term, Term], varnames : List[AnySym],
      W : Typ[V],
      change: Change[V])(ctx: Context[Term, V] = Context.empty[Term]) : Context[Term, V] = {
    ptn match {
      case IdW => ctx // the final co-domain. We have just this for constant constructors.
//      case _ : ConstructorPatterns.Id => ctx
//      case FuncPtn(tail, head) =>
//        val headctx = cnstrContext(head, varnames.tail, W, change)(ctx)
//        change(varnames.head, tail, headctx)
      case CnstFncPtn(tail , head) =>
        val x = tail.symbObj(varnames.head)
        val headctx = cnstrContext(head, varnames.tail, W, change)(ctx)
        x /: headctx
 //     case DepFuncPtn(tail, headfibre , _) =>
 //       val x : Term = tail(W).symbObj(varnames.head)
 //       val headctx = cnstrContext(headfibre(x), varnames.tail, W, change)(ctx)
 //       change(varnames.head, tail, headctx)
      case CnstDepFuncPtn(tail, headfibre , _) =>
        val x = tail.symbObj(varnames.head)
        val headctx = cnstrContext(headfibre(x), varnames.tail, W, change)(ctx)
        x /: headctx
      case _ => ctx
    }
  }

  /**
 * returns symbolic terms of type according to a poly-pattern
 */
  private def symbTerms(ptn : ConstructorPattern[Term, Term], varnames : List[AnySym],
      W : Typ[Term], accum: List[Term] = List()) : List[Term] = ptn match {
    case IdW => accum
    case FuncPtn(tail, head) =>
      val x = tail(W).symbObj(varnames.head)
      symbTerms(head, varnames.tail, W, x :: accum)
    case CnstFncPtn(tail , head) =>
      val x = tail.symbObj(varnames.head)
      symbTerms(head, varnames.tail, W, x :: accum)
    case DepFuncPtn(tail, headfibre , _) =>
      val x : Term = tail(W).symbObj(varnames.head)
      symbTerms(headfibre(x), varnames.tail, W, x :: accum)
    case CnstDepFuncPtn(tail, headfibre , _) =>
      val x : Term = tail.symbObj(varnames.head)
      symbTerms(headfibre(x), varnames.tail, W, x :: accum)
    case _ => accum
  }

  /**
 *  context for recursive definition for a constructor.
 */
  def cnstrRecContext[V<: Term with Subs[V]](f : => (Func[Term, Term]),
      ptn : ConstructorPattern[Term, Term], varnames : List[AnySym],
      W : Typ[V],
      X : Typ[V])(ctx: Context[Term, V] = Context.empty[Term]) : Context[Term, V] = {
    val change = recContextChange[V](f, W, X)
    cnstrContext(ptn, varnames, W, change)(ctx)
  }

  /**
 *  context for inductive definition for a constructor.
 */
  def cnstrIndContext[V<: Term with Subs[V], U <: Term](f : => (FuncLike[Term, Term]),
      ptn : ConstructorPattern[Term, Term]{type ConstructorType = U}, varnames : List[AnySym],
      W : Typ[V],
      Xs :  Term => Typ[V])(ctx: Context[Term, V]) : Context[Term, V] = {
    val change = indContextChange[V](f, W, Xs)
    cnstrContext(ptn, varnames, W, change)(ctx)
  }




  /**
 * formal symbol
 */
  case class RecInduced(cons : Term, func: Term => Term) extends AtomicSym

  /**
 * formal symbol
 */
  case class IndInduced(cons: Term, func: Term => Term) extends AtomicSym

  /**
 * Identities satisfied by the recursion function from f.dom to f.codom.
 */
  case class RecDefinition(f: Func[Term, Term], cs: List[CnstrLHS]){
    val types = for (c <- cs) yield c.recCtxTyp(f)
    val typ = (types :\ f.typ)(_ ->: _)
    val recfn = typ.symbObj(RecSymbol(f.dom, f.codom))

    val consvars = cs map (_.recCtxVar(f))
    val freevars = f :: consvars

    val identitites = cs map ((c) => {
      val lhs = foldterms(recfn, consvars :+ c.arg)
      val rhs = c.recCtxIdRHS(f)
      DefnEqual(lhs, rhs, freevars)
    })
  }

  /**
 * Identities satisfied by the induction dependent function from f.dom to f.depcodom.
 */
  case class IndDefinition(f: FuncLike[Term, Term], cs: List[CnstrLHS]){
    val types = for (c <- cs) yield c.indCtxTyp(f)
    val typ = (types :\ f.typ)(_ ->: _)
    val recfn = typ.symbObj(IndSymbol(f.dom, f.depcodom))

    val consvars = cs map (_.indCtxVar(f))
    val freevars = f :: consvars

    val identitites = cs map ((c) => {
      val lhs = foldterms(recfn, consvars :+ c.arg)
      val rhs = c.indCtxIdRHS(f)
      DefnEqual(lhs, rhs, freevars)
    })
  }


  /**
 * symbolic object for defining a recursion function for functions from W to X.
 */
  case class RecSymbol(W : Typ[Term], X : Typ[Term]) extends AtomicSym

  /**
 * symbolic object for defining an induction function for dependent functions from W to Xs(w: W).
 */
  case class IndSymbol(W : Typ[Term], Xs : Term => Typ[Term]) extends AtomicSym
 */
}
