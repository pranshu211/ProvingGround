package provingGround

import scala.language.implicitConversions 
import scala.util._
import scala.language.existentials
//import scala.reflect.runtime.universe._


// To Do:
//
// Dynamics: Most types.
// Substitution: All types except functions and dependent functions.
// 
// Variables in Pi-types (dependent functions) should be Formal dependent functions - done.
//
// Reformulate: All types are (dependent) functions, inductive types or universes.
// In code: Make an inductive type into a class, with the other types extending this.
//


/** The Homotopy type theory objects, types and utility functions
 *  
 */
object HoTT{
  /** Abstract object */
    trait AbsObj {
      /** The HoTT type */
        val typ: Typ[AbsObj]
        
     /** 
      *     The scala type
      */   
    type ObjTyp = typ.Obj
        
    }
   
    
  
  	/** Substitutions of Var by value in object;
  	 *  should implement for all types, and refine so types map to types
  	 *  one also needs recursion
  	 */
    def substitute[U<: AbsObj, V<: AbsObj](Var: U, value: U)(obj: V): V = (value, obj) match {
      case (Var, _) => obj
      case (valV, Var) => valV.asInstanceOf[V] 
      case (_, FormalAppl(fn : FuncLikeObj[U,V], Var)) => fn(value)
      // Variable and value are functions, the object is this function applied to arg : Need to rework this case
      // This suggests it may be better haing subs in a class, so various associated types are present.
      case (fnVal: FuncLikeObj[_, V], FormalAppl(Var, arg)) => fnVal(arg)
      case (_, Lambda(variable, valu)) => lambda(variable)(substitute(Var, value)(obj)).asInstanceOf[V]
      
      /* Above cases should cover this
      case (_, sym: Symbolic[_]) =>
        sym.name match {
          case fnsym : FormalApplication[_, _,_] =>
        	(fnsym.func, fnsym.arg) match {
        		case (fn, Var) => substitute(Var, value)((fn(value)).asInstanceOf[V]).asInstanceOf[V]
        		case (fn, arg) if fn == Var => substitute(Var, value)((fn(value)).asInstanceOf[V]).asInstanceOf[V]
        		case _ => obj
        	}
          case fnsym : FormalDepApplication[_, _,_] =>
        	(fnsym.func, fnsym.arg) match {
        		case (fn, Var) => substitute(Var, value)((fn(value)).asInstanceOf[V]).asInstanceOf[V]
        		case (fn, arg) if fn == Var => substitute(Var, value)((fn(value)).asInstanceOf[V]).asInstanceOf[V]
        		case _ => obj
        	}
          case _ => obj
        }
        * 
        */
      
      case (valu, pair: AbsPair[_,_]) => mkPair(substitute(Var, valu)(pair.first), substitute(Var, valu)(pair.second)).asInstanceOf[V]
      
      case _ => obj
    }
    
    val subObjs: AbsObj => List[AbsObj] ={
      case pair: AbsPair[_,_] => subObjs(pair.first) ::: subObjs(pair.second)
      case obj: FormalApplication[_,_,_] => subObjs(obj.arg) ::: subObjs(obj.func)
      case eq: IdentityTyp[_] => subObjs(eq.lhs) ::: subObjs(eq.rhs)
      case fnTyp : FuncTyp[_, _, _] => subObjs(fnTyp.dom) ::: subObjs(fnTyp.codom) 
      case Lambda(variable, value) => subObjs(value) map (lambda(variable))
      case PiTyp(fibers) => subObjs(fibers)
      case SigmaTyp(fibers) => subObjs(fibers)
      case FormalAppl(fn, arg) => subObjs(fn) ::: subObjs(arg)
      
      /* Above case should cover this
      case sym: Symbolic[_] =>
        sym.name match {
          case fnsym : FormalApplication[_, _,_] => subObjs(fnsym.arg) ::: subObjs(fnsym.func)
          case fnsym : FormalDepApplication[_, _,_] => subObjs(fnsym.arg) ::: subObjs(fnsym.func)
          case _ => List(sym)
        }
        * /
        */
      case obj: AbsObj => List(obj)
    } 
    
    /** HoTT Type;
     *  The compiler knows that objects have scala-type extending U.
     *  In particular, we can specify that the objects are types, types of types etc.
     */
    trait Typ[+U <: AbsObj] extends AbsObj{
    	/** scala type of objects with this HoTT-type */
        type Obj <: U
        
        /** A symbolic object with specified HoTT type, by default this, and with scala-type Obj*/
        def symbObj[A, W<: AbsObj](name: A, tp: Typ[W] = this): Obj
        
        /** Make symbolic object */
        def ::[A](name:A) = symbObj(name) 
    }
    
    /** Result to be used, not its proof */ 
    class Result[U<: AbsObj](val typ: Typ[U], pf: =>AbsObj) extends AbsObj{
      def proof = pf
    }

    /** traits that are given by name;
     *  does not include, for instance, pairs each of whose instance is given by a name;
     *  same considerations for functions etc.
     */
    trait Symbolic[A]{
      val name: A
      override def toString = name.toString
    }
    
    def sameName[A](sym: A): AbsObj => Boolean = {
      case obj : Symbolic[_] =>
        obj.name match {
          case `sym` => true
          case _ => false
        }
      case _ => false
    }
    
    /** Constructing symbolic objects that are AbsObj but no more refined*/
    case class SymbObj[A, +U<: AbsObj](name: A, typ: Typ[U]) extends AbsObj with Symbolic[A]{
      override def toString = name.toString+" : "+typ.toString
    } 
    
    /** Symbolic types, which the compiler knows are types.
     *  The base tells the scala type of objects and gives a factory for symbolic objects of this scala type.
     */
    case class SymbTyp[A, U<:AbsObj, T<: AbsObj](name: A, univ: Typ[T], base: Typ[U]) extends Typ[U] with Symbolic[A]{
      lazy val typ = univ
      
      type Obj = base.Obj
      
      def symbObj[B, W <: AbsObj](name: B, tp: Typ[W] ) = base.symbObj(name, tp)
      
      override def toString = name.toString+" : "+typ.toString
      
      def elem = this
    }
    
    case class SymbLogicTyp[A](name: A) extends LogicalTyp with Symbolic[A]{
      override def toString = name.toString+" : "+typ.toString
    }

    /** HoTT types with underlying scala type AbsObj.
     *  They belong to the LogicalUniv
     */
    class LogicalTyp extends Typ[AbsObj]{
      
      type Obj = AbsObj
      
      lazy val typ = LogicalUniv
      
      def symbObj[A, W <: AbsObj](name: A, tp: Typ[W]): AbsObj = SymbObj(name, tp)
      
      /** returns function type A -> B */
      def -->[U <: AbsObj](that: Typ[U]) = FuncTyp[AbsObj, LogicalTyp, U](this, that)
 
      def ~>[U <: AbsObj](fibers: TypFamily[AbsObj, LogicalTyp, U]) = PiTyp(fibers)
    }
    
    /** Universes. Can be just a type so far */
    trait Univ[U<:AbsObj] extends Typ[Typ[U]]
    
    /** Inductive construction of the next universe */
    case class NextUniv[U<: AbsObj](base: Typ[U]) extends Univ[U]{
      lazy val typ = NextUniv[Typ[U]](this)
      
      type Obj = Typ[U]
      
      def symbObj[A, W<: AbsObj](name: A, tp: Typ[W])= SymbTyp(name, tp, base)
    }
    
    /** The first universe, consisting of logical types */
    object LogicalUniv extends Univ[AbsObj]{
      lazy val typ = NextUniv[AbsObj](this)
      
      type Obj = LogicalTyp
      
      def symbObj[A, W<: AbsObj](name: A, tp: Typ[W]) = SymbLogicTyp(name)
      
      override def toString ="__"
    }
    
    val __ = LogicalUniv
    
    /** Pair of types (A, B) */
    case class PairTyp[U<: AbsObj, V <: AbsObj](first: Typ[U], second: Typ[V]) extends 
						Typ[AbsPair[U, V]] with AbsPair[Typ[U], Typ[V]]{

    	type Obj = AbsPair[U, V]

		lazy val typ = PairTyp(first.typ, second.typ)

			
			// The name is lost as `name', but can be recovered using pattern matching.
		def symbObj[A, W<: AbsObj](name: A, tp: Typ[W]): AbsPair[U, V] = PairObj(first.symbObj(name), second.symbObj(name))
			}	
    
    /** Object (a, b) in (A, B) */
    case class PairObj[U <: AbsObj, V <: AbsObj](val first: U, val second: V) extends AbsPair[U, V]{
    	lazy val typ = PairTyp(first.typ, second.typ)
					}

    /** Abstract pair, parametrized by scala types of components */
	trait AbsPair[U<: AbsObj, V <: AbsObj] extends AbsObj{
	  val first: U
	  val second: V
	}
	
	def mkPair[U<: AbsObj, V<: AbsObj](f: U, s: V) = (f, s) match{
	  case (fst: Typ[_], scnd: Typ[_]) => PairTyp(fst, scnd)
	  case (fst, scnd) => PairObj(fst, scnd)
	}
    
	/** Function type */
    case class FuncTyp[W<: AbsObj, V <: Typ[W], U<: AbsObj](dom: V, codom: Typ[U]) extends LogicalTyp{
	  override def symbObj[A, T<: AbsObj](name: A, tp: Typ[T]) = FuncSymb[A, W, V, U](name, dom, codom)
	  
	  override def toString = dom.toString + " -> " + codom.toString
	}
    
    trait FuncLikeObj[W <: AbsObj, U <: AbsObj] extends AbsObj with (W => U){
      def apply(arg: W): U
    }
    
	/** a function, i.e.,  an object in a function type */
    trait FuncObj[W<: AbsObj, V<: Typ[W], U<: AbsObj] extends FuncLikeObj[W, U]{
      /** domain*/
	  val dom: V
	  /** codomain */
	  val codom: Typ[U]
	   
	  lazy val typ = FuncTyp[W, V, U](dom, codom)
	  

	  /** Action, i.e., function application */
	  def action(arg:dom.Obj): codom.Obj 
	  
	  /** Function application */
	  def apply(arg: W) = action(arg.asInstanceOf[dom.Obj])
	  
	}

    /** Formal function, i.e., with application purely symbolic. */
	trait FormalFunc[W<: AbsObj, V<: Typ[W], U<: AbsObj] extends FuncObj[W, V, U]{
	  def act(arg: W) = if (arg.typ == dom) Some(codom.symbObj(FormalApplication[W, V, U](this, arg))) else None
	  
	  def action(arg: dom.Obj) = codom.symbObj(FormalApplication[W, V, U](this, arg))
	}
	
	/** Symbol containing function info */
    case class FuncSymb[A, W<: AbsObj, V<: Typ[W], U<: AbsObj](name: A, dom: V, codom: Typ[U]) extends FormalFunc[W, V, U] with Symbolic[A]
	
    trait FormalAppl[W <: AbsObj, U <: AbsObj]{
      val func: FuncLikeObj[W, U]
      
      val arg: W
    }
    
    object FormalAppl{
      def unapply[W <: AbsObj, U <: AbsObj](obj: U): Option[(FuncLikeObj[W, U], W)] =obj match{
        case sym: Symbolic[_] =>
        sym.name match {
          case fnsym : FormalAppl[W, U] => Some(((fnsym.func : FuncLikeObj[W, U]), (fnsym.arg: W)))
          case _ => None
        }
        case _ => None
      }
    }
    
    /** A formal structure representing func(arg) - especially used for pattern matching */
    case class FormalApplication[W<: AbsObj, V<: Typ[W], U<: AbsObj](func: FuncObj[W, V, U], arg: W) extends AbsObj with FormalAppl[W, U]{
      lazy val typ = func.codom
      
      override def toString = func.toString + "("+ arg.toString +")"

    }
	
    /** A function given by a scala function */
	case class FuncDefn[W<: AbsObj, V<: Typ[W], U<: AbsObj](func: AbsObj => U, dom: V, codom: Typ[U]) extends FuncObj[W, V, U]{
	  def act(arg: AbsObj) = if (arg.typ == dom) Some(func(arg)) else None
	  
	  def action(arg: dom.Obj) = func(arg).asInstanceOf[codom.Obj]
	}
	
	/** A lambda-expression.
	 *  variable is mapped to value.
	 *  
	 *  Correction? should allow general variables.
	 *  Should revisit if LogicalTyp variables are too restricitive.
	 */
	case class Lambda[X<: AbsObj, Y <: AbsObj](variable: X, value : Y) extends AbsObj{
	  lazy val typ = (variable.typ.asInstanceOf[LogicalTyp]) --> value.typ
	  
	  def apply(arg: AbsObj) = substitute(variable, arg)(value)
	}
	
	
	/** Lambda constructor
	 *  
	 */		
	def lambda[U<: AbsObj, V <: AbsObj](variable: U)(value : V) = Lambda(variable, value)
	
	/** Context
	 *  
	 */
	trait Context{
	  val givens: Stream[AbsObj]
	  
	  def export(obj: AbsObj): AbsObj
	  
	  def apply(obj: AbsObj) = export(obj)	  
	  
	  def typed[A](name: A) = givens find (sameName(name))
	  
	  def allTyped[A](name: A) = givens filter (sameName(name))
	}
	
	
	/** Context given by chain of lambdas. 
	 *  The first object is the latest
	 */
	case class LambdaContext(givens: Stream[AbsObj]) extends Context{
	  def export(obj: AbsObj) = (obj /: givens)((a, x) => lambda(x)(a))
	  
	  def +(variable: AbsObj) = LambdaContext(variable #:: givens)
	  
	  def assume(ass: AbsObj) = this + ass
	}
	
	
	/** Type family */
	type TypFamily[W<: AbsObj, V<: Typ[W], U<: AbsObj] = FuncObj[W, V, Typ[U]]
	
	/** For all/Product for a type family. This is the type of dependent functions */
	case class PiTyp[W<: AbsObj, V<: Typ[W], U<: AbsObj](fibers: TypFamily[W, V, U]) extends LogicalTyp{
	  override def symbObj[A, T<: AbsObj](name: A, tp: Typ[T]) = DepFuncSymb[A, W, V, U](name, fibers)
	}
	
	/** Exists/Sum for a type family */
	case class SigmaTyp[W<: AbsObj, V<: Typ[W], U<: AbsObj](fibers: TypFamily[W, V, U]) extends LogicalTyp
	
	/** Object in a dependent function type, i.e.,
	 *  a dependent function 
	 */
	trait DepFuncObj[W<: AbsObj, V<: Typ[W], U<: AbsObj] extends FuncLikeObj[W, U]{
	  val fibers: TypFamily[W, V, U] 
	   
	  
	  lazy val typ = PiTyp(fibers)
	  
	  def apply(arg: W) = action(arg.asInstanceOf[fibers.dom.Obj])
	  
	  def action(arg: fibers.dom.Obj): U
	  
	}
	
	/** A formal dependent function, with application symbolic
	 *  
	 */
	trait FormalDepFunc[W<: AbsObj, V<: Typ[W], U<: AbsObj] extends DepFuncObj[W, V, U]{
	  def act(arg: W) = if (arg.typ == fibers.dom) {
	    val typ =fibers(arg) 
	    Some(typ.symbObj(FormalDepApplication[W, V, U](this, arg)).asInstanceOf[U]) 
	  }
	  else None
	  
	  
	  def action(arg: fibers.dom.Obj) = fibers(arg).symbObj(FormalDepApplication[W, V, U](this, arg)).asInstanceOf[U]
	}
	
	
	case class DepFuncSymb[A, W<: AbsObj, V<: Typ[W], U<: AbsObj](name: A, fibers: TypFamily[W, V, U]) extends FormalDepFunc[W, V, U] with Symbolic[A]
	
	/** A symbol capturing formal application
	 *  of a dependent function.
	 */ 
	case class FormalDepApplication[W<: AbsObj, V<: Typ[W], U<: AbsObj](func: DepFuncObj[W, V, U], arg: W) extends AbsObj  with FormalAppl[W, U]{
	  val typFamily = func.fibers
      lazy val typ : Typ[U] = (typFamily(arg)) 
      
      override def toString = func.toString + "("+ arg.toString +")"
    }
	
	/** The identity type. 
	 *  This is the type lhs = rhs
	 */
	case class IdentityTyp[U <: AbsObj](dom: Typ[U], lhs: AbsObj, rhs: AbsObj) extends LogicalTyp
	
	/** A dependent function given by a scala funcion */
	case class DepFuncDefn[W<: AbsObj, V<: Typ[W], U<: AbsObj](func: W => U, dom: V, fibers: TypFamily[W, V,U]) extends DepFuncObj[W, V, U]{
	  def act(arg: W) = if (arg.typ == dom) Some(func(arg)) else None
	  
	  def action(arg: fibers.dom.Obj): U = func(arg)
	}
	
	/** Companion to dependent functions */
	object DepFuncObj{
	  def apply[W<: AbsObj, V<: Typ[W], U<: AbsObj](func: AbsObj => U, dom: V, univ: Univ[U]): DepFuncObj[W, V, U] = {
	    def section(arg: AbsObj) = func(arg).typ.asInstanceOf[Typ[U]]
	    val fibers: FuncObj[W, V, Typ[U]] = FuncDefn[W, V, Typ[U]](section, dom, univ)
	    DepFuncDefn(func, dom, fibers)
	  }
	}
	
	
	val x = 'x' :: __
	
	val y = "y" :: x
	
	/** Symbol factory */	
	def nextChar(s: Set[Char]) = if (s.isEmpty) 'a' else (s.max + 1).toChar
  
	/** Helper for symbol factory */
	def usedChars(s: Set[AbsObj]): Set[Char] = {
	    def charOpt (obj:AbsObj) : Option[Char] = obj match {
	      case sym: Symbolic[_] => Some(Try(sym.name.asInstanceOf[Char]).toOption).flatten
	      case _ => None
	    }
	    
	    
	    s collect (Function.unlift(charOpt _))
	}
	
}














/** Deprecated object
 *  
 */

object HoTTinner{
  
   
    trait AbsObj {
      val typ: Typ[AbsObj]
      
      def as[U <: AbsObj](that: Typ[U]) = {assert (typ==that); this.asInstanceOf[that.Obj]}

	  def as(that: LogicalTyp) = {assert (typ==that); this.asInstanceOf[that.LogicalObj]}
     }
         

    trait Elem[+A]{def elem: A}
    
    implicit def baseElem[A](b: Elem[A]): A = b.elem
    
     
    
	
 	   
//	implicit class AsLogicalTyp[U<: AbsObj](typ: Typ[U]) extends LogicalTyp{
//		implicit class AsLogicalObj(obj: Obj) extends LogicalObj
//		} 

	trait Symbolic[A]{
	    val name: A
//	    val tpe: 
	}

    trait EffectiveTyp[+U <: AbsObj] extends Typ[U]{self =>
      /** Object given by its name */
      def symbObj[A](id: A): Obj   
      
      implicit def asObj(sym: Symbol) = symbObj(sym)

      def ::(sym: Symbol) = symbObj(sym)
    }
    
    case class FnSym[F, A](func: F, arg: A){
      override def toString = func.toString+"("+arg.toString+")"
    }
    
    /** Any type that exists for a reason better than being a universe */
    trait LogicalTyp extends EffectiveTyp[AbsObj]{
    	
    	lazy val typ: Typ[AbsObj] = LogicalUniv

		def -->[V <: AbsObj](that: EffectiveTyp[V]) = FuncTyp(this, that)

		case class SymbObj[A](name: A) extends LogicalObj with Symbolic[A]{
			override def toString = name.toString + " : " + typ.toString
		}
			
		def symbObj[A](id: A) : LogicalObj = SymbObj(id)
			
		case class ApplObj[F, A](func: F, arg: A) extends LogicalObj{
			override def toString = func.toString+"("+arg.toString+")"
		}

		def applObj[F, A](f: F, a: A) = ApplObj(f,a)
									
		class LogicalObj extends Obj{
			def elem : AbsObj = this		
			}

    }

		case class PairTyp[U<: AbsObj, V <: AbsObj](first: EffectiveTyp[U], second: EffectiveTyp[V]) extends 
						EffectiveTyp[AbsPair[U, V]]{

			lazy val typ = PairTyp(first, second)

			class PairObj(val fst: first.Obj, val scnd: second.Obj) extends Obj{
					def elem = ObjPair(fst.elem, scnd.elem)
					}

			def symbObj[A](id: A): Obj = new PairObj(first.symbObj(id), second.symbObj(id))

   
			}	

		trait AbsPair[U<: AbsObj, V <: AbsObj] extends AbsObj

		case class ObjPair[U <: AbsObj, V <: AbsObj](fst: U, scnd: V) extends AbsPair[U, V]{
			lazy val typ = PairTyp(fst.typ.asInstanceOf[EffectiveTyp[U]], scnd.typ.asInstanceOf[EffectiveTyp[V]])
			}

		implicit def BinFunc[U<: AbsObj, V <: AbsObj, W <: AbsObj](first: EffectiveTyp[U], 
															second: EffectiveTyp[V],
                                                              target: EffectiveTyp[W])(
						defn : (first.Obj, second.Obj) => target.Obj) = {
						val fn = FuncTyp(PairTyp(first, second), target)
						val castDefn: fn.dom.Obj => fn.codom.Obj = {
								arg: fn.dom.Obj => defn(arg.asInstanceOf[(first.Obj, second.Obj)]._1,
																													arg.asInstanceOf[(first.Obj, second.Obj)]._2).as(fn.codom)}
						fn.Defn(castDefn)
						}


		trait Univ[+U<: AbsObj] extends EffectiveTyp[Typ[U]]{
			trait TypObj extends Obj with Typ[U]{
				def elem: Typ[U] = this
			}

			} 
	
     
    
    object LogicalUniv extends Typ[LogicalTyp] with Univ[AbsObj]{
      class TypObj extends Obj with LogicalTyp{
        def elem: LogicalTyp = this
        override lazy val typ = LogicalUniv
      }
      
      case class SymbObj[A](name: A) extends TypObj with Symbolic[A]{
			override def toString = name.toString + ":" + typ.toString
		}

      def symbObj[A](name: A) = SymbObj(name)
      
      lazy val typ = NextUniv[Typ[AbsObj], Typ[Typ[AbsObj]]]
      
      implicit class InUniv(tp: LogicalTyp) extends TypObj
      
      override def toString = " _ "
    }
    
	val __ = LogicalUniv

	val A = 'a :: __

	'b :: A

	case class NextUniv[U <: AbsObj, V <: Typ[U]]() extends Typ[V] with Univ[U]{
		lazy val typ = NextUniv[V, Typ[V]]
		
		implicit class InUniv(tp: Typ[U]) extends TypObj
			
		case class SymbObj[A](name: A) extends TypObj

		def symbObj[A](name: A) = SymbObj(name)
    }
    

    
    trait Typ[+U <: AbsObj] extends AbsObj {self =>
           
      val typ: Typ[AbsObj]   
   

      

      trait Obj extends AbsObj with Elem[U]{
        def elem : U 
        
        lazy val typ = self
        
        val obj = this
        
        
        val id = Identity(this, this)
        
        case object refl extends id.LogicalObj
        
        def =:=(that: Obj) = Identity(this, that)
        
        def =:=(that:AbsObj) = eql(typ)(this)(that)		 
      }
      
   
      
      def :::(obj: AbsObj) = obj.as(this)
      
      def :::(tryobj: Try[AbsObj])=Try(tryobj.get as this)
      
	  def ::(obj: AbsObj) = obj.as(this).elem

      case class Identity(left: Obj, right: Obj) extends LogicalTyp      
    }

	case class Logical[T](implicit tag: scala.reflect.runtime.universe.TypeTag[T]) extends LogicalTyp{
		implicit class AsLogicalObj(ob: T) extends LogicalObj
		
		def ::(t: T) = AsLogicalObj(t)
		} 

	
    implicit def me(arg: AbsObj): arg.typ.Obj = arg ::: arg.typ
    
    
    
    def eql(typ: Typ[AbsObj])(left: AbsObj)(right: AbsObj) = typ.Identity(left as typ, right as typ) 
    
    3 :: Logical[Long]
    
    trait AbsFunc[+U <: AbsObj] extends AbsObj{
      val domain: Typ[U];
      def apply(obj: AbsObj): AbsObj
    }
    
	
    case class IndexTyp(tp: Typ[AbsObj]){
      case class ForSection[+U <: AbsObj](section: tp.Obj => EffectiveTyp[U]) extends LogicalTyp{
        case class Index(arg: tp.Obj){
          val codomain = section(arg)

          
          case class DepPair(value: codomain.Obj) extends DepPairTyp.LogicalObj
        }
        
        case object DepFnTyp extends LogicalTyp{
	
        	trait FunctionalObj extends LogicalObj with AbsFunc[AbsObj]{
        		def act(arg: tp.Obj) = {
        			val domain = Index(arg)

							domain.codomain.symbObj(FnSym(this, arg))
        		}
          
        		def apply(arg:tp.Obj) = act(arg)
          
        		def apply(arg: AbsObj) = act(arg.as(tp))
        	}
        	
        }
        
        case object DepPairTyp extends LogicalTyp{
//          def ::(indx: tp.Obj, obj: AbsObj) = {
//            val indxTyp = Index(indx)
//            indxTyp.DepPair(obj as indxTyp.codomain)
//          }
        
        }
      }            
    }
    
    implicit def indexTyp(tp: Typ[AbsObj]) = IndexTyp(tp) 
    
    def Pi[U<: AbsObj](base: Typ[AbsObj])(section:base.Obj => EffectiveTyp[U]) ={
    	val indx = IndexTyp(base)
    	val sect = (x: indx.tp.Obj) => section(x as base)
    	indx.ForSection(sect).DepFnTyp
      }
    
    def Sigma[U <: AbsObj](base: Typ[AbsObj])(section:base.Obj => EffectiveTyp[U]) ={
    	val indx = IndexTyp(base)
    	val sect = (x: indx.tp.Obj) => section(x as base)
    	indx.ForSection(sect).DepPairTyp
      }
 
    
	case class FuncTyp[U<: AbsObj, V <: AbsObj](dom: Typ[U], codom: EffectiveTyp[V]) extends LogicalTyp{
	  //  case class Appl(f: Obj, arg: dom.Obj) extends codom.LogicalObj{
  	//    override val freeVars = arg.freeVars ++ f.freeVars
    //    }     
      
      override def toString = dom.toString+" -> "+codom.toString
      
	  case class LogicalFunction[A](sym: A) extends FunctionalObj{
        def act(arg: dom.Obj) : codom.Obj = codom.symbObj(FnSym(sym, arg))
      }
      
	  override def symbObj[A](sym: A) = LogicalFunction(sym)

      trait FunctionalObj extends LogicalObj  with AbsFunc[AbsObj]{
        val domain = dom
        
        def act(arg: dom.Obj) : codom.Obj  
        
        def apply(arg: dom.Obj): codom.Obj  =  act(arg)
        
        def apply(arg: AbsObj): codom.Obj = act(arg.as(dom))
        
        def apply(tryarg: Try[AbsObj]) = Try(act(tryarg.get as dom))
        
       
      }
       
      implicit class Defn(defn: dom.Obj => codom.Obj) extends FunctionalObj{ 
          def act(arg: dom.Obj) = defn(arg)
      }
      
      case class Lambda(variable: dom.Obj, value: AbsObj) extends LogicalObj
       
    }
     

	def lambda[U<: AbsObj, V <: AbsObj](dom: Typ[U], codom: EffectiveTyp[V])(variable: dom.Obj, value: codom.Obj) ={
					val fnTyp = FuncTyp(dom, codom)
					fnTyp.Lambda(variable.asInstanceOf[fnTyp.dom.Obj], value as fnTyp.codom)
				}

 

 
	def mk(a: Typ[AbsObj], b: Typ[AbsObj]): a.Obj => b.Obj = {assert(a==b); (x:a.Obj) => x.asInstanceOf[b.Obj]}

  //  val idtest = ('n :: Nat) --> ('n :: Nat) 
	
	def nextChar(s: Set[Char]) = if (s.isEmpty) 'a' else (s.max + 1).toChar
  
	def usedChars(s: Set[AbsObj]): Set[Char] = {
	    def charOpt (obj:AbsObj) : Option[Char] = obj match {
	      case sym: Symbolic[_] => Some(Try(sym.name.asInstanceOf[Char]).toOption).flatten
	      case _ => None
	    }
	    
	    
	    s collect (Function.unlift(charOpt _))
	}
	
  object Nat{
    case object ZeroTyp extends LogicalTyp
    
    case object OneTyp extends LogicalTyp
    
    case object star extends OneTyp.LogicalObj
    
    trait ConstructorDomain{
      val dom: LogicalTyp => LogicalTyp 
      
      def apply(that: LogicalTyp) = dom(that)
    }
    
    
    case class ConstContrDom(typ: LogicalTyp) extends ConstructorDomain{
      val dom = (that: LogicalTyp) => typ
    }
    
    case class ToThis(domdom: ConstructorDomain) extends ConstructorDomain{
      val dom = (that: LogicalTyp) =>  domdom.dom(that) --> that
    }
    
    case class InductiveTyp(constructors: Map[Symbol, ConstructorDomain]) extends LogicalTyp{
      val constrs = constructors map (_._2)
      
      def rec(that: LogicalTyp) = {}
    }
    

    
    case object Nat extends LogicalTyp{
      case class Rec(that: LogicalTyp){
        val fnTyp = FuncTyp(Nat, that)
        type domTyp = fnTyp.dom.Obj
        val tgt = Nat --> (that --> that)
//        case class rec(base: that.Obj, step: tgt.Obj) extends fnTyp.LogicalFunction{
//          val atZero = this(zero.as(fnTyp.dom)).as(that)
//          val baseIdtyp = that.Identity(atZero, base)
//          case object baseId extends baseIdtyp.LogicalObj
//        }
        
      }
      
//      def rec[U <: AbsObj, V<: AbsObj](base: AbsObj)(step: FuncTyp[U, V]) = step.typ match {
//        case FuncTyp(Nat, FuncTyp(that : LogicalTyp, other)) if that == other =>
//        val R = Rec(that)
//        val stp = step.asInstanceOf[R.tgt.Obj]
//        R.rec(base as R.that, stp)
//      }
    }
    
    case object zero extends Nat.LogicalObj 
    
    case class succ(n: Nat.Obj) extends Nat.LogicalObj
    
    val one = succ(zero)
 
  }
    
  
}
