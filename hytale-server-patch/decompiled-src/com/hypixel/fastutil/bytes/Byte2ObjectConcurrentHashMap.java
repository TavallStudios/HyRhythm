package com.hypixel.fastutil.bytes;

import com.hypixel.fastutil.FastCollection;
import com.hypixel.fastutil.util.SneakyThrow;
import com.hypixel.fastutil.util.TLRUtil;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import sun.misc.Unsafe;

public class Byte2ObjectConcurrentHashMap<V> {
   protected static final long serialVersionUID = 7249069246763182397L;
   protected static final int MAXIMUM_CAPACITY = 1073741824;
   protected static final int DEFAULT_CAPACITY = 16;
   protected static final int MAX_ARRAY_SIZE = 2147483639;
   protected static final int DEFAULT_CONCURRENCY_LEVEL = 16;
   protected static final float LOAD_FACTOR = 0.75F;
   protected static final int TREEIFY_THRESHOLD = 8;
   protected static final int UNTREEIFY_THRESHOLD = 6;
   protected static final int MIN_TREEIFY_CAPACITY = 64;
   protected static final int MIN_TRANSFER_STRIDE = 16;
   protected static int RESIZE_STAMP_BITS = 16;
   protected static final int MAX_RESIZERS;
   protected static final int RESIZE_STAMP_SHIFT;
   protected static final int MOVED = -1;
   protected static final int TREEBIN = -2;
   protected static final int RESERVED = -3;
   protected static final int HASH_BITS = 2147483647;
   protected static final int NCPU;
   protected transient volatile Node<V>[] table;
   protected transient volatile Node<V>[] nextTable;
   protected transient volatile long baseCount;
   protected transient volatile int sizeCtl;
   protected transient volatile int transferIndex;
   protected transient volatile int cellsBusy;
   protected transient volatile CounterCell[] counterCells;
   protected transient KeySetView<V> keySet;
   protected transient ValuesView<V> values;
   protected transient EntrySetView<V> entrySet;
   protected final byte EMPTY;
   protected static final Unsafe U;
   protected static final long SIZECTL;
   protected static final long TRANSFERINDEX;
   protected static final long BASECOUNT;
   protected static final long CELLSBUSY;
   protected static final long CELLVALUE;
   protected static final long ABASE;
   protected static final int ASHIFT;

   protected static final int spread(int h) {
      return (h ^ h >>> 16) & 2147483647;
   }

   protected static final int tableSizeFor(int c) {
      int n = c - 1;
      n |= n >>> 1;
      n |= n >>> 2;
      n |= n >>> 4;
      n |= n >>> 8;
      n |= n >>> 16;
      return n < 0 ? 1 : (n >= 1073741824 ? 1073741824 : n + 1);
   }

   protected static final <V> Node<V> tabAt(Node<V>[] tab, int i) {
      return (Node)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
   }

   protected static final <V> boolean casTabAt(Node<V>[] tab, int i, Node<V> c, Node<V> v) {
      return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
   }

   protected static final <V> void setTabAt(Node<V>[] tab, int i, Node<V> v) {
      U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
   }

   public Byte2ObjectConcurrentHashMap() {
      this.EMPTY = -1;
   }

   public Byte2ObjectConcurrentHashMap(boolean nonce, byte emptyValue) {
      this.EMPTY = emptyValue;
   }

   public Byte2ObjectConcurrentHashMap(int initialCapacity) {
      this(initialCapacity, true, (byte)-1);
   }

   public Byte2ObjectConcurrentHashMap(int initialCapacity, boolean nonce, byte emptyValue) {
      if (initialCapacity < 0) {
         throw new IllegalArgumentException();
      } else {
         int cap = initialCapacity >= 536870912 ? 1073741824 : tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1);
         this.sizeCtl = cap;
         this.EMPTY = emptyValue;
      }
   }

   public Byte2ObjectConcurrentHashMap(Map<? extends Byte, ? extends V> m, byte emptyValue) {
      this.sizeCtl = 16;
      this.EMPTY = emptyValue;
      this.putAll(m);
   }

   public Byte2ObjectConcurrentHashMap(Byte2ObjectConcurrentHashMap<? extends V> m) {
      this.sizeCtl = 16;
      this.EMPTY = m.EMPTY;
      this.putAll(m);
   }

   public Byte2ObjectConcurrentHashMap(Byte2ObjectMap<V> m) {
      this.sizeCtl = 16;
      this.EMPTY = -1;
      this.putAll(m);
   }

   public Byte2ObjectConcurrentHashMap(Byte2ObjectMap<V> m, byte emptyValue) {
      this.sizeCtl = 16;
      this.EMPTY = emptyValue;
      this.putAll(m);
   }

   public Byte2ObjectConcurrentHashMap(int initialCapacity, float loadFactor) {
      this(initialCapacity, loadFactor, 1, (byte)-1);
   }

   public Byte2ObjectConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, byte emptyValue) {
      if (loadFactor > 0.0F && initialCapacity >= 0 && concurrencyLevel > 0) {
         if (initialCapacity < concurrencyLevel) {
            initialCapacity = concurrencyLevel;
         }

         long size = (long)(1.0 + (double)((float)((long)initialCapacity) / loadFactor));
         int cap = size >= 1073741824L ? 1073741824 : tableSizeFor((int)size);
         this.sizeCtl = cap;
         this.EMPTY = emptyValue;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public int size() {
      long n = this.sumCount();
      return n < 0L ? 0 : (n > 2147483647L ? 2147483647 : (int)n);
   }

   public boolean isEmpty() {
      return this.sumCount() <= 0L;
   }

   public V get(byte key) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         int h = spread(Byte.hashCode(key));
         Node<V>[] tab;
         Node<V> e;
         int n;
         if ((tab = this.table) != null && (n = tab.length) > 0 && (e = tabAt(tab, n - 1 & h)) != null) {
            int eh;
            if ((eh = e.hash) == h) {
               byte ek;
               if ((ek = e.key) == key || ek != this.EMPTY && key == ek) {
                  return e.val;
               }
            } else if (eh < 0) {
               Node<V> p;
               return (V)((p = e.find(h, key)) != null ? p.val : null);
            }

            while((e = e.next) != null) {
               byte ek;
               if (e.hash == h && ((ek = e.key) == key || ek != this.EMPTY && key == ek)) {
                  return e.val;
               }
            }
         }

         return null;
      }
   }

   public boolean containsKey(byte key) {
      return this.get(key) != null;
   }

   public boolean containsValue(Object value) {
      if (value == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label87:
               while(true) {
                  if (p != null) {
                     next = p;
                     break;
                  }

                  if (baseIndex < baseLimit) {
                     Node<V>[] t = tab;
                     int n;
                     if (tab != null && (n = tab.length) > index && index >= 0) {
                        if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                           continue;
                        }

                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              continue label87;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
                  break;
               }

               if (p == null) {
                  break;
               }

               V v;
               if ((v = p.val) == value || v != null && value.equals(v)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public V put(byte key, V value) {
      return (V)this.putVal(key, value, false);
   }

   protected final V putVal(byte key, V value, boolean onlyIfAbsent) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value == null) {
         throw new NullPointerException();
      } else {
         int hash = spread(Byte.hashCode(key));
         int binCount = 0;
         Node<V>[] tab = this.table;

         while(true) {
            int n;
            while(tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & hash)) == null) {
               if (casTabAt(tab, i, (Node)null, new Node(this.EMPTY, hash, key, value, (Node)null))) {
                  break;
               }
            } else {
               int fh;
               if ((fh = f.hash) == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  V oldVal = null;
                  synchronized(f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof TreeBin) {
                              binCount = 2;
                              Node<V> p;
                              if ((p = (f).putTreeVal(hash, key, value)) != null) {
                                 oldVal = p.val;
                                 if (!onlyIfAbsent) {
                                    p.val = value;
                                 }
                              }
                           }
                        } else {
                           label107: {
                              binCount = 1;

                              Node<V> e;
                              byte ek;
                              for(e = f; e.hash != hash || (ek = e.key) != key && (ek == this.EMPTY || key != ek); ++binCount) {
                                 Node<V> pred = e;
                                 if ((e = e.next) == null) {
                                    pred.next = new Node<V>(this.EMPTY, hash, key, value, (Node)null);
                                    break label107;
                                 }
                              }

                              oldVal = e.val;
                              if (!onlyIfAbsent) {
                                 e.val = value;
                              }
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }

                     if (oldVal != null) {
                        return oldVal;
                     }
                     break;
                  }
               }
            }
         }

         this.addCount(1L, binCount);
         return null;
      }
   }

   public void putAll(Map<? extends Byte, ? extends V> m) {
      this.tryPresize(m.size());

      for(Map.Entry<? extends Byte, ? extends V> e : m.entrySet()) {
         this.putVal((Byte)e.getKey(), e.getValue(), false);
      }

   }

   public void putAll(Byte2ObjectConcurrentHashMap<? extends V> m) {
      this.tryPresize(m.size());
      ObjectIterator var2 = m.byte2ObjectEntrySet().iterator();

      while(var2.hasNext()) {
         Byte2ObjectMap.Entry<? extends V> e = (Byte2ObjectMap.Entry)var2.next();
         this.putVal(e.getByteKey(), e.getValue(), false);
      }

   }

   public void putAll(Byte2ObjectMap<V> m) {
      this.tryPresize(m.size());
      ObjectIterator<? extends Byte2ObjectMap.Entry<? extends V>> iterator = m.byte2ObjectEntrySet().iterator();

      while(iterator.hasNext()) {
         Byte2ObjectMap.Entry<? extends V> next = (Byte2ObjectMap.Entry)iterator.next();
         this.putVal(next.getByteKey(), next.getValue(), false);
      }

   }

   public V remove(byte key) {
      return (V)this.replaceNode(key, (Object)null, (Object)null);
   }

   /** @deprecated */
   @Deprecated
   public V remove(Byte key) {
      return (V)this.replaceNode(key, (Object)null, (Object)null);
   }

   /** @deprecated */
   @Deprecated
   public V remove(Object key) {
      return (V)this.remove((Byte)key);
   }

   protected final V replaceNode(byte key, V value, Object cv) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         int hash = spread(Byte.hashCode(key));
         Node<V>[] tab = this.table;

         Node<V> f;
         int n;
         int i;
         while(tab != null && (n = tab.length) != 0 && (f = tabAt(tab, i = n - 1 & hash)) != null) {
            int fh;
            if ((fh = f.hash) == -1) {
               tab = this.helpTransfer(tab, f);
            } else {
               V oldVal = null;
               boolean validated = false;
               synchronized(f) {
                  if (tabAt(tab, i) == f) {
                     if (fh < 0) {
                        if (f instanceof TreeBin) {
                           validated = true;
                           TreeBin<V> t = (TreeBin)f;
                           TreeNode<V> r;
                           TreeNode<V> p;
                           if ((r = t.root) != null && (p = r.findTreeNode(hash, key, (Class)null)) != null) {
                              V pv = p.val;
                              if (cv == null || cv == pv || pv != null && cv.equals(pv)) {
                                 oldVal = pv;
                                 if (value != null) {
                                    p.val = value;
                                 } else if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        }
                     } else {
                        label126: {
                           validated = true;
                           Node<V> e = f;
                           Node<V> pred = null;

                           byte ek;
                           while(e.hash != hash || (ek = e.key) != key && (ek == this.EMPTY || key != ek)) {
                              pred = e;
                              if ((e = e.next) == null) {
                                 break label126;
                              }
                           }

                           V ev = e.val;
                           if (cv == null || cv == ev || ev != null && cv.equals(ev)) {
                              oldVal = ev;
                              if (value != null) {
                                 e.val = value;
                              } else if (pred != null) {
                                 pred.next = e.next;
                              } else {
                                 setTabAt(tab, i, e.next);
                              }
                           }
                        }
                     }
                  }
               }

               if (validated) {
                  if (oldVal != null) {
                     if (value == null) {
                        this.addCount(-1L, -1);
                     }

                     return oldVal;
                  }
                  break;
               }
            }
         }

         return null;
      }
   }

   public void clear() {
      long delta = 0L;
      int i = 0;
      Node<V>[] tab = this.table;

      while(tab != null && i < tab.length) {
         Node<V> f = tabAt(tab, i);
         if (f == null) {
            ++i;
         } else {
            int fh;
            if ((fh = f.hash) == -1) {
               tab = this.helpTransfer(tab, f);
               i = 0;
            } else {
               synchronized(f) {
                  if (tabAt(tab, i) == f) {
                     for(Node<V> p = (Node<V>)(fh >= 0 ? f : (f instanceof TreeBin ? ((TreeBin)f).first : null)); p != null; p = p.next) {
                        --delta;
                     }

                     setTabAt(tab, i++, (Node)null);
                  }
               }
            }
         }
      }

      if (delta != 0L) {
         this.addCount(delta, -1);
      }

   }

   public KeySetView<V> keySet() {
      KeySetView<V> ks;
      return (ks = this.keySet) != null ? ks : (this.keySet = this.buildKeySetView());
   }

   protected KeySetView<V> buildKeySetView() {
      return new KeySetView<V>(this, (Object)null);
   }

   public FastCollection<V> values() {
      ValuesView<V> vs;
      return (vs = this.values) != null ? vs : (this.values = this.buildValuesView());
   }

   protected ValuesView<V> buildValuesView() {
      return new ValuesView<V>(this);
   }

   public ObjectSet<Byte2ObjectMap.Entry<V>> byte2ObjectEntrySet() {
      EntrySetView<V> es;
      return (es = this.entrySet) != null ? es : (this.entrySet = this.buildEntrySetView());
   }

   /** @deprecated */
   @Deprecated
   public ObjectSet<Map.Entry<Byte, V>> entrySet() {
      return this.byte2ObjectEntrySet();
   }

   protected EntrySetView<V> buildEntrySetView() {
      return new EntrySetView<V>(this);
   }

   public int hashCode() {
      int h = 0;
      Node<V>[] tt;
      if ((tt = this.table) != null) {
         Node<V>[] tab = tt;
         Node<V> next = null;
         TableStack<V> stack = null;
         TableStack<V> spare = null;
         int index = 0;
         int baseIndex = 0;
         int baseLimit = tt.length;
         int baseSize = tt.length;

         while(true) {
            Node<V> p = null;
            p = next;
            if (next != null) {
               p = next.next;
            }

            label77: {
               while(true) {
                  if (p != null) {
                     next = p;
                     break label77;
                  }

                  if (baseIndex >= baseLimit) {
                     break;
                  }

                  Node<V>[] t = tab;
                  int n;
                  if (tab == null || (n = tab.length) <= index || index < 0) {
                     break;
                  }

                  if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                     if (p instanceof ForwardingNode) {
                        tab = ((ForwardingNode)p).nextTable;
                        p = null;
                        TableStack<V> s = spare;
                        if (spare != null) {
                           spare = spare.next;
                        } else {
                           s = new TableStack<V>();
                        }

                        s.tab = t;
                        s.length = n;
                        s.index = index;
                        s.next = stack;
                        stack = s;
                        continue;
                     }

                     if (p instanceof TreeBin) {
                        p = ((TreeBin)p).first;
                     } else {
                        p = null;
                     }
                  }

                  if (stack == null) {
                     if ((index += baseSize) >= n) {
                        ++baseIndex;
                        index = baseIndex;
                     }
                  } else {
                     while(true) {
                        TableStack<V> s = stack;
                        int len;
                        if (stack == null || (index += len = stack.length) < n) {
                           if (stack == null && (index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                           break;
                        }

                        n = len;
                        index = stack.index;
                        tab = stack.tab;
                        stack.tab = null;
                        TableStack<V> anext = stack.next;
                        stack.next = spare;
                        stack = anext;
                        spare = s;
                     }
                  }
               }

               next = null;
            }

            if (p == null) {
               break;
            }

            h += Byte.hashCode(p.key) ^ p.val.hashCode();
         }
      }

      return h;
   }

   public String toString() {
      Node<V>[] t;
      int f = (t = this.table) == null ? 0 : t.length;
      Traverser<V> it = new Traverser<V>(t, f, 0, f);
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      Node<V> p;
      if ((p = it.advance()) != null) {
         while(true) {
            byte k = p.key;
            V v = p.val;
            sb.append(k);
            sb.append('=');
            sb.append(v == this ? "(this Map)" : v);
            if ((p = it.advance()) == null) {
               break;
            }

            sb.append(',').append(' ');
         }
      }

      return sb.append('}').toString();
   }

   public boolean equals(Object o) {
      if (o != this) {
         if (!(o instanceof Byte2ObjectConcurrentHashMap)) {
            return false;
         }

         Byte2ObjectConcurrentHashMap<?> m = (Byte2ObjectConcurrentHashMap)o;
         Node<V>[] t;
         int f = (t = this.table) == null ? 0 : t.length;
         Traverser<V> it = new Traverser<V>(t, f, 0, f);

         Node<V> p;
         while((p = it.advance()) != null) {
            V val = p.val;
            Object v = m.get(p.key);
            if (v == null || v != val && !v.equals(val)) {
               return false;
            }
         }

         ObjectIterator var11 = m.byte2ObjectEntrySet().iterator();

         while(var11.hasNext()) {
            Byte2ObjectMap.Entry<?> e = (Byte2ObjectMap.Entry)var11.next();
            Object mv;
            Object v;
            byte mk;
            if ((mk = e.getByteKey()) == m.EMPTY || (mv = e.getValue()) == null || (v = this.get(mk)) == null || mv != v && !mv.equals(v)) {
               return false;
            }
         }
      }

      return true;
   }

   public V putIfAbsent(byte key, V value) {
      return (V)this.putVal(key, value, true);
   }

   public boolean remove(byte key, Object value) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else {
         return value != null && this.replaceNode(key, (Object)null, value) != null;
      }
   }

   public boolean replace(byte key, V oldValue, V newValue) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (oldValue != null && newValue != null) {
         return this.replaceNode(key, newValue, oldValue) != null;
      } else {
         throw new NullPointerException();
      }
   }

   public V replace(byte key, V value) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value == null) {
         throw new NullPointerException();
      } else {
         return (V)this.replaceNode(key, value, (Object)null);
      }
   }

   public V getOrDefault(byte key, V defaultValue) {
      V v;
      return (V)((v = (V)this.get(key)) == null ? defaultValue : v);
   }

   public int forEach(ByteObjConsumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEach(ByteBiObjConsumer<? super V, X> action, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, x);
               ++count;
            }
         }

         return count;
      }
   }

   public <X, Y> int forEach(ByteTriObjConsumer<? super V, X, Y> action, X x, Y y) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, x, y);
               ++count;
            }
         }

         return count;
      }
   }

   public int forEachWithByte(ByteObjByteConsumer<? super V> action, byte ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               ++count;
            }
         }

         return count;
      }
   }

   public int forEachWithShort(ByteObjShortConsumer<? super V> action, short ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               ++count;
            }
         }

         return count;
      }
   }

   public int forEachWithInt(ByteObjIntConsumer<? super V> action, int ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               ++count;
            }
         }

         return count;
      }
   }

   public int forEachWithLong(ByteObjLongConsumer<? super V> action, long ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               ++count;
            }
         }

         return count;
      }
   }

   public int forEachWithFloat(ByteObjFloatConsumer<? super V> action, float ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               ++count;
            }
         }

         return count;
      }
   }

   public int forEachWithDouble(ByteObjDoubleConsumer<? super V> action, double ii) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithByte(ByteBiObjByteConsumer<? super V, X> action, byte ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithShort(ByteBiObjShortConsumer<? super V, X> action, short ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithInt(ByteBiObjIntConsumer<? super V, X> action, int ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithLong(ByteBiObjLongConsumer<? super V, X> action, long ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithFloat(ByteBiObjFloatConsumer<? super V, X> action, float ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               ++count;
            }
         }

         return count;
      }
   }

   public <X> int forEachWithDouble(ByteBiObjDoubleConsumer<? super V, X> action, double ii, X x) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         int count = 0;
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label80: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label80;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               action.accept(p.key, p.val, ii, x);
               ++count;
            }
         }

         return count;
      }
   }

   public void replaceAll(Byte2ObjectOperator<V> function) {
      if (function == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label88: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label88;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               V oldValue = p.val;
               byte key = p.key;

               while(true) {
                  V newValue = function.apply(key, oldValue);
                  if (newValue == null) {
                     throw new NullPointerException();
                  }

                  if (this.replaceNode(key, newValue, oldValue) != null || (oldValue = (V)this.get(key)) == null) {
                     break;
                  }
               }
            }
         }

      }
   }

   public V computeIfAbsent(byte key, ByteFunction<? extends V> mappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (mappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Byte.hashCode(key));
         V val = null;
         int binCount = 0;
         Node<V>[] tab = this.table;

         while(true) {
            int n;
            while(tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               Node<V> r = new ReservationNode<V>(this.EMPTY);
               synchronized(r) {
                  if (casTabAt(tab, i, (Node)null, r)) {
                     binCount = 1;
                     Node<V> node = null;

                     try {
                        if ((val = mappingFunction.apply(key)) != null) {
                           node = new Node<V>(this.EMPTY, h, key, val, (Node)null);
                        }
                     } finally {
                        setTabAt(tab, i, node);
                     }
                  }
               }

               if (binCount != 0) {
                  break;
               }
            } else {
               int fh;
               if ((fh = f.hash) == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  boolean added = false;
                  synchronized(f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof TreeBin) {
                              binCount = 2;
                              TreeBin<V> t = (TreeBin)f;
                              TreeNode<V> p;
                              TreeNode<V> r;
                              if ((r = t.root) != null && (p = r.findTreeNode(h, key, (Class)null)) != null) {
                                 val = p.val;
                              } else if ((val = mappingFunction.apply(key)) != null) {
                                 added = true;
                                 t.putTreeVal(h, key, val);
                              }
                           }
                        } else {
                           label274: {
                              binCount = 1;

                              Node<V> e;
                              byte ek;
                              for(e = f; e.hash != h || (ek = e.key) != key && (ek == this.EMPTY || key != ek); ++binCount) {
                                 Node<V> pred = e;
                                 if ((e = e.next) == null) {
                                    if ((val = mappingFunction.apply(key)) != null) {
                                       added = true;
                                       pred.next = new Node<V>(this.EMPTY, h, key, val, (Node)null);
                                    }
                                    break label274;
                                 }
                              }

                              val = e.val;
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }

                     if (!added) {
                        return val;
                     }
                     break;
                  }
               }
            }
         }

         if (val != null) {
            this.addCount(1L, binCount);
         }

         return val;
      }
   }

   public V computeIfPresent(byte key, ByteObjFunction<? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (remappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Byte.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Node<V>[] tab = this.table;

         while(true) {
            int n;
            while(tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               break;
            }

            int fh;
            if ((fh = f.hash) == -1) {
               tab = this.helpTransfer(tab, f);
            } else {
               synchronized(f) {
                  if (tabAt(tab, i) == f) {
                     if (fh < 0) {
                        if (f instanceof TreeBin) {
                           binCount = 2;
                           TreeBin<V> t = (TreeBin)f;
                           TreeNode<V> r;
                           TreeNode<V> p;
                           if ((r = t.root) != null && (p = r.findTreeNode(h, key, (Class)null)) != null) {
                              val = remappingFunction.apply(key, p.val);
                              if (val != null) {
                                 p.val = val;
                              } else {
                                 delta = -1;
                                 if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        }
                     } else {
                        label111: {
                           binCount = 1;
                           Node<V> e = f;

                           Node<V> pred;
                           byte ek;
                           for(pred = null; e.hash != h || (ek = e.key) != key && (ek == this.EMPTY || key != ek); ++binCount) {
                              pred = e;
                              if ((e = e.next) == null) {
                                 break label111;
                              }
                           }

                           val = remappingFunction.apply(key, e.val);
                           if (val != null) {
                              e.val = val;
                           } else {
                              delta = -1;
                              Node<V> en = e.next;
                              if (pred != null) {
                                 pred.next = en;
                              } else {
                                 setTabAt(tab, i, en);
                              }
                           }
                        }
                     }
                  }
               }

               if (binCount != 0) {
                  break;
               }
            }
         }

         if (delta != 0) {
            this.addCount((long)delta, binCount);
         }

         return val;
      }
   }

   public V compute(byte key, ByteObjFunction<? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (remappingFunction == null) {
         throw new NullPointerException();
      } else {
         int h = spread(Byte.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Node<V>[] tab = this.table;

         while(true) {
            int n;
            while(tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               Node<V> r = new ReservationNode<V>(this.EMPTY);
               synchronized(r) {
                  if (casTabAt(tab, i, (Node)null, r)) {
                     binCount = 1;
                     Node<V> node = null;

                     try {
                        if ((val = remappingFunction.apply(key, (Object)null)) != null) {
                           delta = 1;
                           node = new Node<V>(this.EMPTY, h, key, val, (Node)null);
                        }
                     } finally {
                        setTabAt(tab, i, node);
                     }
                  }
               }

               if (binCount != 0) {
                  break;
               }
            } else {
               int fh;
               if ((fh = f.hash) == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  synchronized(f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof TreeBin) {
                              binCount = 1;
                              TreeBin<V> t = (TreeBin)f;
                              TreeNode<V> r;
                              TreeNode<V> p;
                              if ((r = t.root) != null) {
                                 p = r.findTreeNode(h, key, (Class)null);
                              } else {
                                 p = null;
                              }

                              V pv = (V)(p == null ? null : p.val);
                              val = remappingFunction.apply(key, pv);
                              if (val != null) {
                                 if (p != null) {
                                    p.val = val;
                                 } else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                 }
                              } else if (p != null) {
                                 delta = -1;
                                 if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        } else {
                           label301: {
                              binCount = 1;
                              Node<V> e = f;

                              Node<V> pred;
                              byte ek;
                              for(pred = null; e.hash != h || (ek = e.key) != key && (ek == this.EMPTY || key != ek); ++binCount) {
                                 pred = e;
                                 if ((e = e.next) == null) {
                                    val = remappingFunction.apply(key, (Object)null);
                                    if (val != null) {
                                       delta = 1;
                                       pred.next = new Node<V>(this.EMPTY, h, key, val, (Node)null);
                                    }
                                    break label301;
                                 }
                              }

                              val = remappingFunction.apply(key, e.val);
                              if (val != null) {
                                 e.val = val;
                              } else {
                                 delta = -1;
                                 Node<V> en = e.next;
                                 if (pred != null) {
                                    pred.next = en;
                                 } else {
                                    setTabAt(tab, i, en);
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }
                     break;
                  }
               }
            }
         }

         if (delta != 0) {
            this.addCount((long)delta, binCount);
         }

         return val;
      }
   }

   public V merge(byte key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      if (key == this.EMPTY) {
         throw new IllegalArgumentException("Key is EMPTY: " + this.EMPTY);
      } else if (value != null && remappingFunction != null) {
         int h = spread(Byte.hashCode(key));
         V val = null;
         int delta = 0;
         int binCount = 0;
         Node<V>[] tab = this.table;

         while(true) {
            int n;
            while(tab == null || (n = tab.length) == 0) {
               tab = this.initTable();
            }

            Node<V> f;
            int i;
            if ((f = tabAt(tab, i = n - 1 & h)) == null) {
               if (casTabAt(tab, i, (Node)null, new Node(this.EMPTY, h, key, value, (Node)null))) {
                  delta = 1;
                  val = value;
                  break;
               }
            } else {
               int fh;
               if ((fh = f.hash) == -1) {
                  tab = this.helpTransfer(tab, f);
               } else {
                  synchronized(f) {
                     if (tabAt(tab, i) == f) {
                        if (fh < 0) {
                           if (f instanceof TreeBin) {
                              binCount = 2;
                              TreeBin<V> t = (TreeBin)f;
                              TreeNode<V> r = t.root;
                              TreeNode<V> p = r == null ? null : r.findTreeNode(h, key, (Class)null);
                              val = (V)(p == null ? value : remappingFunction.apply(p.val, value));
                              if (val != null) {
                                 if (p != null) {
                                    p.val = val;
                                 } else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                 }
                              } else if (p != null) {
                                 delta = -1;
                                 if (t.removeTreeNode(p)) {
                                    setTabAt(tab, i, this.untreeify(t.first));
                                 }
                              }
                           }
                        } else {
                           label132: {
                              binCount = 1;
                              Node<V> e = f;

                              Node<V> pred;
                              byte ek;
                              for(pred = null; e.hash != h || (ek = e.key) != key && (ek == this.EMPTY || key != ek); ++binCount) {
                                 pred = e;
                                 if ((e = e.next) == null) {
                                    delta = 1;
                                    val = value;
                                    pred.next = new Node<V>(this.EMPTY, h, key, value, (Node)null);
                                    break label132;
                                 }
                              }

                              val = (V)remappingFunction.apply(e.val, value);
                              if (val != null) {
                                 e.val = val;
                              } else {
                                 delta = -1;
                                 Node<V> en = e.next;
                                 if (pred != null) {
                                    pred.next = en;
                                 } else {
                                    setTabAt(tab, i, en);
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (binCount != 0) {
                     if (binCount >= 8) {
                        this.treeifyBin(tab, i);
                     }
                     break;
                  }
               }
            }
         }

         if (delta != 0) {
            this.addCount((long)delta, binCount);
         }

         return val;
      } else {
         throw new NullPointerException();
      }
   }

   public long mappingCount() {
      long n = this.sumCount();
      return n < 0L ? 0L : n;
   }

   public static ByteSet newKeySet() {
      return new KeySetView(new Byte2ObjectConcurrentHashMap(), Boolean.TRUE);
   }

   public static KeySetView<Boolean> newKeySet(int initialCapacity) {
      return new KeySetView<Boolean>(new Byte2ObjectConcurrentHashMap(initialCapacity), Boolean.TRUE);
   }

   public KeySetView<V> keySet(V mappedValue) {
      if (mappedValue == null) {
         throw new NullPointerException();
      } else {
         return new KeySetView<V>(this, mappedValue);
      }
   }

   protected static final int resizeStamp(int n) {
      return Integer.numberOfLeadingZeros(n) | 1 << RESIZE_STAMP_BITS - 1;
   }

   protected final Node<V>[] initTable() {
      Node<V>[] tab;
      while((tab = this.table) == null || tab.length == 0) {
         int sc;
         if ((sc = this.sizeCtl) < 0) {
            Thread.yield();
         } else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
               if ((tab = this.table) == null || tab.length == 0) {
                  int n = sc > 0 ? sc : 16;
                  Node<V>[] nt = new Node[n];
                  tab = nt;
                  this.table = nt;
                  sc = n - (n >>> 2);
               }
               break;
            } finally {
               this.sizeCtl = sc;
            }
         }
      }

      return tab;
   }

   protected final void addCount(long x, int check) {
      CounterCell[] as;
      long b;
      long s;
      if ((as = this.counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = this.baseCount, s = b + x)) {
         boolean uncontended = true;
         CounterCell a;
         long v;
         int m;
         if (as == null || (m = as.length - 1) < 0 || (a = as[TLRUtil.getProbe() & m]) == null || !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            this.fullAddCount(x, uncontended);
            return;
         }

         if (check <= 1) {
            return;
         }

         s = this.sumCount();
      }

      int n;
      Node<V>[] tab;
      int sc;
      if (check >= 0) {
         for(; s >= (long)(sc = this.sizeCtl) && (tab = this.table) != null && (n = tab.length) < 1073741824; s = this.sumCount()) {
            int rs = resizeStamp(n);
            if (sc < 0) {
               Node<V>[] nt;
               if (sc >>> RESIZE_STAMP_SHIFT != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS || (nt = this.nextTable) == null || this.transferIndex <= 0) {
                  break;
               }

               if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                  this.transfer(tab, nt);
               }
            } else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2)) {
               this.transfer(tab, (Node[])null);
            }
         }
      }

   }

   protected final Node<V>[] helpTransfer(Node<V>[] tab, Node<V> f) {
      Node<V>[] nextTab;
      if (tab != null && f instanceof ForwardingNode && (nextTab = ((ForwardingNode)f).nextTable) != null) {
         int rs = resizeStamp(tab.length);

         int sc;
         while(nextTab == this.nextTable && this.table == tab && (sc = this.sizeCtl) < 0 && sc >>> RESIZE_STAMP_SHIFT == rs && sc != rs + 1 && sc != rs + MAX_RESIZERS && this.transferIndex > 0) {
            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
               this.transfer(tab, nextTab);
               break;
            }
         }

         return nextTab;
      } else {
         return this.table;
      }
   }

   protected final void tryPresize(int size) {
      int c = size >= 536870912 ? 1073741824 : tableSizeFor(size + (size >>> 1) + 1);

      int sc;
      while((sc = this.sizeCtl) >= 0) {
         Node<V>[] tab = this.table;
         int n;
         if (tab != null && (n = tab.length) != 0) {
            if (c <= sc || n >= 1073741824) {
               break;
            }

            if (tab == this.table) {
               int rs = resizeStamp(n);
               if (sc < 0) {
                  Node<V>[] nt;
                  if (sc >>> RESIZE_STAMP_SHIFT != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS || (nt = this.nextTable) == null || this.transferIndex <= 0) {
                     break;
                  }

                  if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                     this.transfer(tab, nt);
                  }
               } else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2)) {
                  this.transfer(tab, (Node[])null);
               }
            }
         } else {
            n = sc > c ? sc : c;
            if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
               try {
                  if (this.table == tab) {
                     Node<V>[] nt = new Node[n];
                     this.table = nt;
                     sc = n - (n >>> 2);
                  }
               } finally {
                  this.sizeCtl = sc;
               }
            }
         }
      }

   }

   protected final void transfer(Node<V>[] tab, Node<V>[] nextTab) {
      int n = tab.length;
      int stride;
      if ((stride = NCPU > 1 ? (n >>> 3) / NCPU : n) < 16) {
         stride = 16;
      }

      if (nextTab == null) {
         try {
            Node<V>[] nt = new Node[n << 1];
            nextTab = nt;
         } catch (Throwable var27) {
            this.sizeCtl = 2147483647;
            return;
         }

         this.nextTable = nextTab;
         this.transferIndex = n;
      }

      int nextn = nextTab.length;
      ForwardingNode<V> fwd = new ForwardingNode<V>(this.EMPTY, nextTab);
      boolean advance = true;
      boolean finishing = false;
      int i = 0;
      int bound = 0;

      while(true) {
         while(!advance) {
            if (i >= 0 && i < n && i + n < nextn) {
               Node<V> f;
               if ((f = tabAt(tab, i)) == null) {
                  advance = casTabAt(tab, i, (Node)null, fwd);
               } else {
                  int fh;
                  if ((fh = f.hash) == -1) {
                     advance = true;
                  } else {
                     synchronized(f) {
                        if (tabAt(tab, i) == f) {
                           if (fh >= 0) {
                              int runBit = fh & n;
                              Node<V> lastRun = f;

                              for(Node<V> p = f.next; p != null; p = p.next) {
                                 int b = p.hash & n;
                                 if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                 }
                              }

                              Node<V> ln;
                              Node<V> hn;
                              if (runBit == 0) {
                                 ln = lastRun;
                                 hn = null;
                              } else {
                                 hn = lastRun;
                                 ln = null;
                              }

                              for(Node<V> p = f; p != lastRun; p = p.next) {
                                 int ph = p.hash;
                                 byte pk = p.key;
                                 V pv = p.val;
                                 if ((ph & n) == 0) {
                                    ln = new Node<V>(this.EMPTY, ph, pk, pv, ln);
                                 } else {
                                    hn = new Node<V>(this.EMPTY, ph, pk, pv, hn);
                                 }
                              }

                              setTabAt(nextTab, i, ln);
                              setTabAt(nextTab, i + n, hn);
                              setTabAt(tab, i, fwd);
                              advance = true;
                           } else if (f instanceof TreeBin) {
                              TreeBin<V> t = (TreeBin)f;
                              TreeNode<V> lo = null;
                              TreeNode<V> loTail = null;
                              TreeNode<V> hi = null;
                              TreeNode<V> hiTail = null;
                              int lc = 0;
                              int hc = 0;

                              for(Node<V> e = t.first; e != null; e = e.next) {
                                 int h = e.hash;
                                 TreeNode<V> p = new TreeNode<V>(this.EMPTY, h, e.key, e.val, (Node)null, (TreeNode)null);
                                 if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null) {
                                       lo = p;
                                    } else {
                                       loTail.next = p;
                                    }

                                    loTail = p;
                                    ++lc;
                                 } else {
                                    if ((p.prev = hiTail) == null) {
                                       hi = p;
                                    } else {
                                       hiTail.next = p;
                                    }

                                    hiTail = p;
                                    ++hc;
                                 }
                              }

                              Node<V> ln = (Node<V>)(lc <= 6 ? this.untreeify(lo) : (hc != 0 ? new TreeBin(this.EMPTY, lo) : t));
                              Node<V> hn = (Node<V>)(hc <= 6 ? this.untreeify(hi) : (lc != 0 ? new TreeBin(this.EMPTY, hi) : t));
                              setTabAt(nextTab, i, ln);
                              setTabAt(nextTab, i + n, hn);
                              setTabAt(tab, i, fwd);
                              advance = true;
                           }
                        }
                     }
                  }
               }
            } else {
               if (finishing) {
                  this.nextTable = null;
                  this.table = nextTab;
                  this.sizeCtl = (n << 1) - (n >>> 1);
                  return;
               }

               int sc;
               if (U.compareAndSwapInt(this, SIZECTL, sc = this.sizeCtl, sc - 1)) {
                  if (sc - 2 != resizeStamp(n) << RESIZE_STAMP_SHIFT) {
                     return;
                  }

                  advance = true;
                  finishing = true;
                  i = n;
               }
            }
         }

         --i;
         if (i < bound && !finishing) {
            int nextIndex;
            if ((nextIndex = this.transferIndex) <= 0) {
               i = -1;
               advance = false;
            } else {
               int nextBound;
               if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex, nextBound = nextIndex > stride ? nextIndex - stride : 0)) {
                  bound = nextBound;
                  i = nextIndex - 1;
                  advance = false;
               }
            }
         } else {
            advance = false;
         }
      }
   }

   protected final long sumCount() {
      CounterCell[] as = this.counterCells;
      long sum = this.baseCount;
      if (as != null) {
         for(int i = 0; i < as.length; ++i) {
            CounterCell a;
            if ((a = as[i]) != null) {
               sum += a.value;
            }
         }
      }

      return sum;
   }

   protected final void fullAddCount(long x, boolean wasUncontended) {
      int h;
      if ((h = TLRUtil.getProbe()) == 0) {
         TLRUtil.localInit();
         h = TLRUtil.getProbe();
         wasUncontended = true;
      }

      boolean collide = false;

      while(true) {
         CounterCell[] as;
         int n;
         if ((as = this.counterCells) != null && (n = as.length) > 0) {
            CounterCell a;
            if ((a = as[n - 1 & h]) == null) {
               if (this.cellsBusy == 0) {
                  CounterCell r = new CounterCell(x);
                  if (this.cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                     boolean created = false;

                     try {
                        CounterCell[] rs;
                        int m;
                        int j;
                        if ((rs = this.counterCells) != null && (m = rs.length) > 0 && rs[j = m - 1 & h] == null) {
                           rs[j] = r;
                           created = true;
                        }
                     } finally {
                        this.cellsBusy = 0;
                     }

                     if (created) {
                        break;
                     }
                     continue;
                  }
               }

               collide = false;
            } else if (!wasUncontended) {
               wasUncontended = true;
            } else {
               long v;
               if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x)) {
                  break;
               }

               if (this.counterCells == as && n < NCPU) {
                  if (!collide) {
                     collide = true;
                  } else if (this.cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                     try {
                        if (this.counterCells == as) {
                           CounterCell[] rs = new CounterCell[n << 1];

                           for(int i = 0; i < n; ++i) {
                              rs[i] = as[i];
                           }

                           this.counterCells = rs;
                        }
                     } finally {
                        this.cellsBusy = 0;
                     }

                     collide = false;
                     continue;
                  }
               } else {
                  collide = false;
               }
            }

            h = TLRUtil.advanceProbe(h);
         } else if (this.cellsBusy == 0 && this.counterCells == as && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
            boolean init = false;

            try {
               if (this.counterCells == as) {
                  CounterCell[] rs = new CounterCell[2];
                  rs[h & 1] = new CounterCell(x);
                  this.counterCells = rs;
                  init = true;
               }
            } finally {
               this.cellsBusy = 0;
            }

            if (init) {
               break;
            }
         } else {
            long v;
            if (U.compareAndSwapLong(this, BASECOUNT, v = this.baseCount, v + x)) {
               break;
            }
         }
      }

   }

   protected final void treeifyBin(Node<V>[] tab, int index) {
      if (tab != null) {
         int n;
         if ((n = tab.length) < 64) {
            this.tryPresize(n << 1);
         } else {
            Node<V> b;
            if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
               synchronized(b) {
                  if (tabAt(tab, index) == b) {
                     TreeNode<V> hd = null;
                     TreeNode<V> tl = null;

                     for(Node<V> e = b; e != null; e = e.next) {
                        TreeNode<V> p = new TreeNode<V>(this.EMPTY, e.hash, e.key, e.val, (Node)null, (TreeNode)null);
                        if ((p.prev = tl) == null) {
                           hd = p;
                        } else {
                           tl.next = p;
                        }

                        tl = p;
                     }

                     setTabAt(tab, index, new TreeBin(this.EMPTY, hd));
                  }
               }
            }
         }
      }

   }

   protected <V> Node<V> untreeify(Node<V> b) {
      Node<V> hd = null;
      Node<V> tl = null;

      for(Node<V> q = b; q != null; q = q.next) {
         Node<V> p = new Node<V>(this.EMPTY, q.hash, q.key, q.val, (Node)null);
         if (tl == null) {
            hd = p;
         } else {
            tl.next = p;
         }

         tl = p;
      }

      return hd;
   }

   protected final int batchFor(long b) {
      long n;
      if (b != 9223372036854775807L && (n = this.sumCount()) > 1L && n >= b) {
         int sp = ForkJoinPool.getCommonPoolParallelism() << 2;
         long n;
         return b > 0L && (n = n / b) < (long)sp ? (int)n : sp;
      } else {
         return 0;
      }
   }

   public void forEach(long parallelismThreshold, ByteObjConsumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         (new ForEachMappingTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, action)).invoke();
      }
   }

   public <U> void forEach(long parallelismThreshold, ByteObjFunction<? super V, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         (new ForEachTransformedMappingTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U search(long parallelismThreshold, ByteObjFunction<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return (U)(new SearchMappingsTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference())).invoke();
      }
   }

   public <U> U search(ByteObjFunction<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U, X> U search(ByteBiObjFunction<? super V, X, ? extends U> searchFunction, X x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithByte(ByteObjByteFunction<? super V, ? extends U> searchFunction, byte x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithShort(ByteObjShortFunction<? super V, ? extends U> searchFunction, short x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithInt(ByteObjIntFunction<? super V, ? extends U> searchFunction, int x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithLong(ByteObjLongFunction<? super V, ? extends U> searchFunction, long x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithFloat(ByteObjFloatFunction<? super V, ? extends U> searchFunction, float x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U searchWithDouble(ByteObjDoubleFunction<? super V, ? extends U> searchFunction, double x) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         Node<V>[] tt;
         if ((tt = this.table) != null) {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label83: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label83;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  break;
               }

               U u = searchFunction.apply(p.key, p.val, x);
               if (u != null) {
                  return u;
               }
            }
         }

         return null;
      }
   }

   public <U> U reduce(long parallelismThreshold, ByteObjFunction<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         return (U)(new MapReduceMappingsTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceMappingsTask)null, transformer, reducer)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U reduce(ByteObjFunction<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         Node<V>[] tt;
         if ((tt = this.table) == null) {
            return null;
         } else {
            Node<V>[] tab = tt;
            Node<V> next = null;
            TableStack<V> stack = null;
            TableStack<V> spare = null;
            int index = 0;
            int baseIndex = 0;
            int baseLimit = tt.length;
            int baseSize = tt.length;
            U r = null;

            while(true) {
               Node<V> p = null;
               p = next;
               if (next != null) {
                  p = next.next;
               }

               label90: {
                  while(true) {
                     if (p != null) {
                        next = p;
                        break label90;
                     }

                     if (baseIndex >= baseLimit) {
                        break;
                     }

                     Node<V>[] t = tab;
                     int n;
                     if (tab == null || (n = tab.length) <= index || index < 0) {
                        break;
                     }

                     if ((p = tabAt(tab, index)) != null && p.hash < 0) {
                        if (p instanceof ForwardingNode) {
                           tab = ((ForwardingNode)p).nextTable;
                           p = null;
                           TableStack<V> s = spare;
                           if (spare != null) {
                              spare = spare.next;
                           } else {
                              s = new TableStack<V>();
                           }

                           s.tab = t;
                           s.length = n;
                           s.index = index;
                           s.next = stack;
                           stack = s;
                           continue;
                        }

                        if (p instanceof TreeBin) {
                           p = ((TreeBin)p).first;
                        } else {
                           p = null;
                        }
                     }

                     if (stack == null) {
                        if ((index += baseSize) >= n) {
                           ++baseIndex;
                           index = baseIndex;
                        }
                     } else {
                        while(true) {
                           TableStack<V> s = stack;
                           int len;
                           if (stack == null || (index += len = stack.length) < n) {
                              if (stack == null && (index += baseSize) >= n) {
                                 ++baseIndex;
                                 index = baseIndex;
                              }
                              break;
                           }

                           n = len;
                           index = stack.index;
                           tab = stack.tab;
                           stack.tab = null;
                           TableStack<V> anext = stack.next;
                           stack.next = spare;
                           stack = anext;
                           spare = s;
                        }
                     }
                  }

                  next = null;
               }

               if (p == null) {
                  return r;
               }

               U u;
               if ((u = transformer.apply(p.key, p.val)) != null) {
                  r = (U)(r == null ? u : reducer.apply(r, u));
               }
            }
         }
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceToDouble(long parallelismThreshold, ToDoubleByteObjFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceMappingsToDoubleTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceMappingsToDoubleTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceToLong(long parallelismThreshold, ToLongByteObjFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceMappingsToLongTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceMappingsToLongTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceToInt(long parallelismThreshold, ToIntByteObjFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceMappingsToIntTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceMappingsToIntTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachKey(long parallelismThreshold, ByteConsumer action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         (new ForEachKeyTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, action)).invoke();
      }
   }

   public <U> void forEachKey(long parallelismThreshold, ByteFunction<? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         (new ForEachTransformedKeyTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchKeys(long parallelismThreshold, ByteFunction<? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return (U)(new SearchKeysTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference())).invoke();
      }
   }

   public byte reduceKeys(long parallelismThreshold, ByteReduceTaskOperator reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return (new ReduceKeysTask(this.EMPTY, (BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (ReduceKeysTask)null, reducer)).invoke0();
      }
   }

   public <U> U reduceKeys(long parallelismThreshold, ByteFunction<? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         return (U)(new MapReduceKeysTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceKeysTask)null, transformer, reducer)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceKeysToDouble(long parallelismThreshold, ByteToDoubleFunction transformer, double basis, DoubleBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceKeysToDoubleTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceKeysToDoubleTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceKeysToLong(long parallelismThreshold, ByteToLongFunction transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceKeysToLongTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceKeysToLongTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceKeysToInt(long parallelismThreshold, ByteToIntFunction transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceKeysToIntTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceKeysToIntTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         (new ForEachValueTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, action)).invoke();
      }
   }

   public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         (new ForEachTransformedValueTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return (U)(new SearchValuesTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference())).invoke();
      }
   }

   public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return (V)(new ReduceValuesTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (ReduceValuesTask)null, reducer)).invoke();
      }
   }

   public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         return (U)(new MapReduceValuesTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceValuesTask)null, transformer, reducer)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceValuesToDoubleTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceValuesToDoubleTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceValuesToLongTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceValuesToLongTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceValuesToIntTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceValuesToIntTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public void forEachEntry(long parallelismThreshold, Consumer<? super Entry<V>> action) {
      if (action == null) {
         throw new NullPointerException();
      } else {
         (new ForEachEntryTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, action)).invoke();
      }
   }

   public <U> void forEachEntry(long parallelismThreshold, Function<Entry<V>, ? extends U> transformer, Consumer<? super U> action) {
      if (transformer != null && action != null) {
         (new ForEachTransformedEntryTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, transformer, action)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public <U> U searchEntries(long parallelismThreshold, Function<Entry<V>, ? extends U> searchFunction) {
      if (searchFunction == null) {
         throw new NullPointerException();
      } else {
         return (U)(new SearchEntriesTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference())).invoke();
      }
   }

   public Entry<V> reduceEntries(long parallelismThreshold, BiFunction<Entry<V>, Entry<V>, ? extends Entry<V>> reducer) {
      if (reducer == null) {
         throw new NullPointerException();
      } else {
         return (Entry)(new ReduceEntriesTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (ReduceEntriesTask)null, reducer)).invoke();
      }
   }

   public <U> U reduceEntries(long parallelismThreshold, Function<Entry<V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
      if (transformer != null && reducer != null) {
         return (U)(new MapReduceEntriesTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceEntriesTask)null, transformer, reducer)).invoke();
      } else {
         throw new NullPointerException();
      }
   }

   public double reduceEntriesToDouble(long parallelismThreshold, ToDoubleFunction<Entry<V>> transformer, double basis, DoubleBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceEntriesToDoubleTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceEntriesToDoubleTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public long reduceEntriesToLong(long parallelismThreshold, ToLongFunction<Entry<V>> transformer, long basis, LongBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceEntriesToLongTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceEntriesToLongTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public int reduceEntriesToInt(long parallelismThreshold, ToIntFunction<Entry<V>> transformer, int basis, IntBinaryOperator reducer) {
      if (transformer != null && reducer != null) {
         return (new MapReduceEntriesToIntTask((BulkTask)null, this.batchFor(parallelismThreshold), 0, 0, this.table, (MapReduceEntriesToIntTask)null, transformer, basis, reducer)).invoke0();
      } else {
         throw new NullPointerException();
      }
   }

   public V valueMatching(Predicate<V> predicate) {
      Node<V> next = null;
      TableStack<V> stack = null;
      TableStack<V> spare = null;
      int index = 0;
      int baseIndex = 0;
      Node<V>[] tab;
      int f = (tab = this.table) == null ? 0 : tab.length;
      int baseLimit = f;
      int baseSize = f;
      boolean b = false;

      label86:
      while(next != null || !b) {
         b |= true;
         Node<V> e = next;
         if (next != null) {
            e = next.next;
         }

         while(true) {
            if (e != null) {
               next = e;
               if (predicate.test(e.val)) {
                  return e.val;
               }
               continue label86;
            }

            if (baseIndex >= baseLimit) {
               break;
            }

            Node<V>[] t = tab;
            int n;
            if (tab == null || (n = tab.length) <= index || index < 0) {
               break;
            }

            if ((e = tabAt(tab, index)) != null && e.hash < 0) {
               if (e instanceof ForwardingNode) {
                  tab = ((ForwardingNode)e).nextTable;
                  e = null;
                  TableStack<V> s = spare;
                  if (spare != null) {
                     spare = spare.next;
                  } else {
                     s = new TableStack<V>();
                  }

                  s.tab = t;
                  s.length = n;
                  s.index = index;
                  s.next = stack;
                  stack = s;
                  continue;
               }

               if (e instanceof TreeBin) {
                  e = ((TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (stack == null) {
               if ((index += baseSize) >= n) {
                  ++baseIndex;
                  index = baseIndex;
               }
            } else {
               while(true) {
                  TableStack<V> s = stack;
                  int len;
                  if (stack == null || (index += len = stack.length) < n) {
                     if (stack == null && (index += baseSize) >= n) {
                        ++baseIndex;
                        index = baseIndex;
                     }
                     break;
                  }

                  n = len;
                  index = stack.index;
                  tab = stack.tab;
                  stack.tab = null;
                  TableStack<V> next1 = stack.next;
                  stack.next = spare;
                  stack = next1;
                  spare = s;
               }
            }
         }

         next = null;
      }

      return null;
   }

   static {
      MAX_RESIZERS = (1 << 32 - RESIZE_STAMP_BITS) - 1;
      RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
      NCPU = Runtime.getRuntime().availableProcessors();

      try {
         Field f = Unsafe.class.getDeclaredField("theUnsafe");
         f.setAccessible(true);
         U = (Unsafe)f.get((Object)null);
         Class<?> k = Byte2ObjectConcurrentHashMap.class;
         SIZECTL = U.objectFieldOffset(k.getDeclaredField("sizeCtl"));
         TRANSFERINDEX = U.objectFieldOffset(k.getDeclaredField("transferIndex"));
         BASECOUNT = U.objectFieldOffset(k.getDeclaredField("baseCount"));
         CELLSBUSY = U.objectFieldOffset(k.getDeclaredField("cellsBusy"));
         Class<?> ck = CounterCell.class;
         CELLVALUE = U.objectFieldOffset(ck.getDeclaredField("value"));
         Class<?> ak = Node[].class;
         ABASE = (long)U.arrayBaseOffset(ak);
         int scale = U.arrayIndexScale(ak);
         if ((scale & scale - 1) != 0) {
            throw new Error("data type scale not a power of two");
         } else {
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
         }
      } catch (Exception e) {
         throw new Error(e);
      }
   }

   protected static class Node<V> implements Entry<V> {
      public final byte EMPTY;
      public final int hash;
      public final byte key;
      public volatile V val;
      public volatile Node<V> next;

      public Node(byte empty, int hash, byte key, V val, Node<V> next) {
         this.EMPTY = empty;
         this.hash = hash;
         this.key = key;
         this.val = val;
         this.next = next;
      }

      public final boolean isEmpty() {
         return this.key == this.EMPTY;
      }

      public final Byte getKey() {
         return this.key;
      }

      public final byte getByteKey() {
         return this.key;
      }

      public final V getValue() {
         return this.val;
      }

      public final int hashCode() {
         return Byte.hashCode(this.key) ^ this.val.hashCode();
      }

      public final String toString() {
         if (this.isEmpty()) {
            return "EMPTY=" + String.valueOf(this.val);
         } else {
            byte var10000 = this.key;
            return var10000 + "=" + String.valueOf(this.val);
         }
      }

      public final V setValue(V value) {
         throw new UnsupportedOperationException();
      }

      public final boolean equals(Object o) {
         boolean empty = this.isEmpty();
         if (o instanceof Entry) {
            if (empty != ((Entry)o).isEmpty()) {
               return false;
            } else if (!empty && this.key != ((Entry)o).getByteKey()) {
               return false;
            } else {
               return this.val.equals(((Entry)o).getValue());
            }
         } else {
            return false;
         }
      }

      protected Node<V> find(int h, byte k) {
         Node<V> e = this;
         if (k != this.EMPTY) {
            do {
               byte ek;
               if (e.hash == h && ((ek = e.key) == k || ek != this.EMPTY && k == ek)) {
                  return e;
               }
            } while((e = e.next) != null);
         }

         return null;
      }
   }

   protected static class Segment<V> extends ReentrantLock implements Serializable {
      public static final long serialVersionUID = 2249069246763182397L;
      public final float loadFactor;

      public Segment(float lf) {
         this.loadFactor = lf;
      }
   }

   protected static final class ForwardingNode<V> extends Node<V> {
      public final Node<V>[] nextTable;

      public ForwardingNode(byte empty, Node<V>[] tab) {
         super(empty, -1, empty, (Object)null, (Node)null);
         this.nextTable = tab;
      }

      protected Node<V> find(int h, byte k) {
         Node<V>[] tab = this.nextTable;

         label41:
         while(true) {
            Node<V> e;
            int n;
            if (k != this.EMPTY && tab != null && (n = tab.length) != 0 && (e = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, n - 1 & h)) != null) {
               int eh;
               byte ek;
               while((eh = e.hash) != h || (ek = e.key) != k && (ek == this.EMPTY || k != ek)) {
                  if (eh < 0) {
                     if (!(e instanceof ForwardingNode)) {
                        return e.find(h, k);
                     }

                     tab = ((ForwardingNode)e).nextTable;
                     continue label41;
                  }

                  if ((e = e.next) == null) {
                     return null;
                  }
               }

               return e;
            }

            return null;
         }
      }
   }

   protected static final class ReservationNode<V> extends Node<V> {
      public ReservationNode(byte empty) {
         super(empty, -3, empty, (Object)null, (Node)null);
      }

      protected Node<V> find(int h, byte k) {
         return null;
      }
   }

   protected static final class CounterCell {
      public volatile long value;

      public CounterCell(long x) {
         this.value = x;
      }
   }

   protected static final class TreeNode<V> extends Node<V> {
      public TreeNode<V> parent;
      public TreeNode<V> left;
      public TreeNode<V> right;
      public TreeNode<V> prev;
      public boolean red;

      public TreeNode(byte empty, int hash, byte key, V val, Node<V> next, TreeNode<V> parent) {
         super(empty, hash, key, val, next);
         this.parent = parent;
      }

      protected Node<V> find(int h, byte k) {
         return this.findTreeNode(h, k, (Class)null);
      }

      protected final TreeNode<V> findTreeNode(int h, byte k, Class<?> kc) {
         if (k != this.EMPTY) {
            TreeNode<V> p = this;

            do {
               TreeNode<V> pl = p.left;
               TreeNode<V> pr = p.right;
               int ph;
               if ((ph = p.hash) > h) {
                  p = pl;
               } else if (ph < h) {
                  p = pr;
               } else {
                  byte pk;
                  if ((pk = p.key) == k || pk != this.EMPTY && k == pk) {
                     return p;
                  }

                  if (pl == null) {
                     p = pr;
                  } else if (pr == null) {
                     p = pl;
                  } else {
                     int dir;
                     if ((dir = Byte.compare(k, pk)) != 0) {
                        p = dir < 0 ? pl : pr;
                     } else {
                        TreeNode<V> q;
                        if ((q = pr.findTreeNode(h, k, kc)) != null) {
                           return q;
                        }

                        p = pl;
                     }
                  }
               }
            } while(p != null);
         }

         return null;
      }
   }

   protected static final class TreeBin<V> extends Node<V> {
      public TreeNode<V> root;
      public volatile TreeNode<V> first;
      public volatile Thread waiter;
      public volatile int lockState;
      public static final int WRITER = 1;
      public static final int WAITER = 2;
      public static final int READER = 4;
      protected static final Unsafe U;
      protected static final long LOCKSTATE;

      protected int tieBreakOrder(byte a, byte b) {
         int comp = Byte.compare(a, b);
         return comp > 0 ? 1 : -1;
      }

      public TreeBin(byte empty, TreeNode<V> b) {
         super(empty, -2, empty, (Object)null, (Node)null);
         this.first = b;
         TreeNode<V> r = null;

         TreeNode<V> next;
         for(TreeNode<V> x = b; x != null; x = next) {
            next = (TreeNode)x.next;
            x.left = x.right = null;
            if (r == null) {
               x.parent = null;
               x.red = false;
               r = x;
            } else {
               byte k = x.key;
               int h = x.hash;
               Class<?> kc = null;
               TreeNode<V> p = r;

               int dir;
               TreeNode<V> xp;
               do {
                  byte pk = p.key;
                  int ph;
                  if ((ph = p.hash) > h) {
                     dir = -1;
                  } else if (ph < h) {
                     dir = 1;
                  } else if ((dir = Byte.compare(k, pk)) == 0) {
                     dir = this.tieBreakOrder(k, pk);
                  }

                  xp = p;
               } while((p = dir <= 0 ? p.left : p.right) != null);

               x.parent = xp;
               if (dir <= 0) {
                  xp.left = x;
               } else {
                  xp.right = x;
               }

               r = this.<V>balanceInsertion(r, x);
            }
         }

         this.root = r;

         assert this.checkInvariants(this.root);

      }

      protected final void lockRoot() {
         if (!U.compareAndSwapInt(this, LOCKSTATE, 0, 1)) {
            this.contendedLock();
         }

      }

      protected final void unlockRoot() {
         this.lockState = 0;
      }

      protected final void contendedLock() {
         boolean waiting = false;

         int s;
         do {
            while(((s = this.lockState) & -3) != 0) {
               if ((s & 2) == 0) {
                  if (U.compareAndSwapInt(this, LOCKSTATE, s, s | 2)) {
                     waiting = true;
                     this.waiter = Thread.currentThread();
                  }
               } else if (waiting) {
                  LockSupport.park(this);
               }
            }
         } while(!U.compareAndSwapInt(this, LOCKSTATE, s, 1));

         if (waiting) {
            this.waiter = null;
         }

      }

      protected final Node<V> find(int h, byte k) {
         if (k != this.EMPTY) {
            Node<V> e = this.first;

            while(e != null) {
               int s;
               if (((s = this.lockState) & 3) != 0) {
                  byte ek;
                  if (e.hash == h && ((ek = e.key) == k || ek != this.EMPTY && k == ek)) {
                     return e;
                  }

                  e = e.next;
               } else if (U.compareAndSwapInt(this, LOCKSTATE, s, s + 4)) {
                  TreeNode<V> p;
                  try {
                     TreeNode<V> r;
                     p = (r = this.root) == null ? null : r.findTreeNode(h, k, (Class)null);
                  } finally {
                     Thread w;
                     if (U.getAndAddInt(this, LOCKSTATE, -4) == 6 && (w = this.waiter) != null) {
                        LockSupport.unpark(w);
                     }

                  }

                  return p;
               }
            }
         }

         return null;
      }

      protected final TreeNode<V> putTreeVal(int h, byte k, V v) {
         Class<?> kc = null;
         boolean searched = false;
         TreeNode<V> p = this.root;

         while(true) {
            if (p == null) {
               this.first = this.root = new TreeNode<V>(this.EMPTY, h, k, v, (Node)null, (TreeNode)null);
            } else {
               int dir;
               int ph;
               if ((ph = p.hash) > h) {
                  dir = -1;
               } else if (ph < h) {
                  dir = 1;
               } else {
                  byte pk;
                  if ((pk = p.key) == k || pk != this.EMPTY && k == pk) {
                     return p;
                  }

                  if ((dir = Byte.compare(k, pk)) == 0) {
                     if (!searched) {
                        searched = true;
                        TreeNode<V> q;
                        TreeNode<V> ch;
                        if ((ch = p.left) != null && (q = ch.findTreeNode(h, k, kc)) != null || (ch = p.right) != null && (q = ch.findTreeNode(h, k, kc)) != null) {
                           return q;
                        }
                     }

                     dir = this.tieBreakOrder(k, pk);
                  }
               }

               TreeNode<V> xp = p;
               if ((p = dir <= 0 ? p.left : p.right) != null) {
                  continue;
               }

               TreeNode<V> f = this.first;
               TreeNode<V> x;
               this.first = x = new TreeNode<V>(this.EMPTY, h, k, v, f, xp);
               if (f != null) {
                  f.prev = x;
               }

               if (dir <= 0) {
                  xp.left = x;
               } else {
                  xp.right = x;
               }

               if (!xp.red) {
                  x.red = true;
               } else {
                  this.lockRoot();

                  try {
                     this.root = this.<V>balanceInsertion(this.root, x);
                  } finally {
                     this.unlockRoot();
                  }
               }
            }

            assert this.checkInvariants(this.root);

            return null;
         }
      }

      protected final boolean removeTreeNode(TreeNode<V> p) {
         TreeNode<V> next = (TreeNode)p.next;
         TreeNode<V> pred = p.prev;
         if (pred == null) {
            this.first = next;
         } else {
            pred.next = next;
         }

         if (next != null) {
            next.prev = pred;
         }

         if (this.first == null) {
            this.root = null;
            return true;
         } else {
            TreeNode<V> r;
            TreeNode<V> rl;
            if ((r = this.root) != null && r.right != null && (rl = r.left) != null && rl.left != null) {
               this.lockRoot();

               try {
                  TreeNode<V> pl = p.left;
                  TreeNode<V> pr = p.right;
                  TreeNode<V> replacement;
                  if (pl != null && pr != null) {
                     TreeNode<V> s;
                     TreeNode<V> sl;
                     for(s = pr; (sl = s.left) != null; s = sl) {
                     }

                     boolean c = s.red;
                     s.red = p.red;
                     p.red = c;
                     TreeNode<V> sr = s.right;
                     TreeNode<V> pp = p.parent;
                     if (s == pr) {
                        p.parent = s;
                        s.right = p;
                     } else {
                        TreeNode<V> sp = s.parent;
                        if ((p.parent = sp) != null) {
                           if (s == sp.left) {
                              sp.left = p;
                           } else {
                              sp.right = p;
                           }
                        }

                        if ((s.right = pr) != null) {
                           pr.parent = s;
                        }
                     }

                     p.left = null;
                     if ((p.right = sr) != null) {
                        sr.parent = p;
                     }

                     if ((s.left = pl) != null) {
                        pl.parent = s;
                     }

                     if ((s.parent = pp) == null) {
                        r = s;
                     } else if (p == pp.left) {
                        pp.left = s;
                     } else {
                        pp.right = s;
                     }

                     if (sr != null) {
                        replacement = sr;
                     } else {
                        replacement = p;
                     }
                  } else if (pl != null) {
                     replacement = pl;
                  } else if (pr != null) {
                     replacement = pr;
                  } else {
                     replacement = p;
                  }

                  if (replacement != p) {
                     TreeNode<V> pp = replacement.parent = p.parent;
                     if (pp == null) {
                        r = replacement;
                     } else if (p == pp.left) {
                        pp.left = replacement;
                     } else {
                        pp.right = replacement;
                     }

                     p.left = p.right = p.parent = null;
                  }

                  this.root = p.red ? r : this.balanceDeletion(r, replacement);
                  TreeNode<V> pp;
                  if (p == replacement && (pp = p.parent) != null) {
                     if (p == pp.left) {
                        pp.left = null;
                     } else if (p == pp.right) {
                        pp.right = null;
                     }

                     p.parent = null;
                  }
               } finally {
                  this.unlockRoot();
               }

               assert this.checkInvariants(this.root);

               return false;
            } else {
               return true;
            }
         }
      }

      protected <V> TreeNode<V> rotateLeft(TreeNode<V> root, TreeNode<V> p) {
         TreeNode<V> r;
         if (p != null && (r = p.right) != null) {
            TreeNode<V> rl;
            if ((rl = p.right = r.left) != null) {
               rl.parent = p;
            }

            TreeNode<V> pp;
            if ((pp = r.parent = p.parent) == null) {
               root = r;
               r.red = false;
            } else if (pp.left == p) {
               pp.left = r;
            } else {
               pp.right = r;
            }

            r.left = p;
            p.parent = r;
         }

         return root;
      }

      protected <V> TreeNode<V> rotateRight(TreeNode<V> root, TreeNode<V> p) {
         TreeNode<V> l;
         if (p != null && (l = p.left) != null) {
            TreeNode<V> lr;
            if ((lr = p.left = l.right) != null) {
               lr.parent = p;
            }

            TreeNode<V> pp;
            if ((pp = l.parent = p.parent) == null) {
               root = l;
               l.red = false;
            } else if (pp.right == p) {
               pp.right = l;
            } else {
               pp.left = l;
            }

            l.right = p;
            p.parent = l;
         }

         return root;
      }

      protected <V> TreeNode<V> balanceInsertion(TreeNode<V> root, TreeNode<V> x) {
         x.red = true;

         TreeNode<V> xp;
         while((xp = x.parent) != null) {
            TreeNode<V> xpp;
            if (!xp.red || (xpp = xp.parent) == null) {
               return root;
            }

            TreeNode<V> xppl;
            if (xp == (xppl = xpp.left)) {
               TreeNode<V> xppr;
               if ((xppr = xpp.right) != null && xppr.red) {
                  xppr.red = false;
                  xp.red = false;
                  xpp.red = true;
                  x = xpp;
               } else {
                  if (x == xp.right) {
                     x = xp;
                     root = this.<V>rotateLeft(root, xp);
                     xpp = (xp = xp.parent) == null ? null : xp.parent;
                  }

                  if (xp != null) {
                     xp.red = false;
                     if (xpp != null) {
                        xpp.red = true;
                        root = this.<V>rotateRight(root, xpp);
                     }
                  }
               }
            } else if (xppl != null && xppl.red) {
               xppl.red = false;
               xp.red = false;
               xpp.red = true;
               x = xpp;
            } else {
               if (x == xp.left) {
                  x = xp;
                  root = this.<V>rotateRight(root, xp);
                  xpp = (xp = xp.parent) == null ? null : xp.parent;
               }

               if (xp != null) {
                  xp.red = false;
                  if (xpp != null) {
                     xpp.red = true;
                     root = this.<V>rotateLeft(root, xpp);
                  }
               }
            }
         }

         x.red = false;
         return x;
      }

      protected <V> TreeNode<V> balanceDeletion(TreeNode<V> root, TreeNode<V> x) {
         while(x != null && x != root) {
            TreeNode<V> xp;
            if ((xp = x.parent) == null) {
               x.red = false;
               return x;
            }

            if (x.red) {
               x.red = false;
               return root;
            }

            TreeNode<V> xpl;
            if ((xpl = xp.left) == x) {
               TreeNode<V> xpr;
               if ((xpr = xp.right) != null && xpr.red) {
                  xpr.red = false;
                  xp.red = true;
                  root = this.<V>rotateLeft(root, xp);
                  xpr = (xp = x.parent) == null ? null : xp.right;
               }

               if (xpr == null) {
                  x = xp;
               } else {
                  TreeNode<V> sl = xpr.left;
                  TreeNode<V> sr = xpr.right;
                  if (sr != null && sr.red || sl != null && sl.red) {
                     if (sr == null || !sr.red) {
                        if (sl != null) {
                           sl.red = false;
                        }

                        xpr.red = true;
                        root = this.<V>rotateRight(root, xpr);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                     }

                     if (xpr != null) {
                        xpr.red = xp == null ? false : xp.red;
                        if ((sr = xpr.right) != null) {
                           sr.red = false;
                        }
                     }

                     if (xp != null) {
                        xp.red = false;
                        root = this.<V>rotateLeft(root, xp);
                     }

                     x = root;
                  } else {
                     xpr.red = true;
                     x = xp;
                  }
               }
            } else {
               if (xpl != null && xpl.red) {
                  xpl.red = false;
                  xp.red = true;
                  root = this.<V>rotateRight(root, xp);
                  xpl = (xp = x.parent) == null ? null : xp.left;
               }

               if (xpl == null) {
                  x = xp;
               } else {
                  TreeNode<V> sl = xpl.left;
                  TreeNode<V> sr = xpl.right;
                  if (sl != null && sl.red || sr != null && sr.red) {
                     if (sl == null || !sl.red) {
                        if (sr != null) {
                           sr.red = false;
                        }

                        xpl.red = true;
                        root = this.<V>rotateLeft(root, xpl);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                     }

                     if (xpl != null) {
                        xpl.red = xp == null ? false : xp.red;
                        if ((sl = xpl.left) != null) {
                           sl.red = false;
                        }
                     }

                     if (xp != null) {
                        xp.red = false;
                        root = this.<V>rotateRight(root, xp);
                     }

                     x = root;
                  } else {
                     xpl.red = true;
                     x = xp;
                  }
               }
            }
         }

         return root;
      }

      protected <V> boolean checkInvariants(TreeNode<V> t) {
         TreeNode<V> tp = t.parent;
         TreeNode<V> tl = t.left;
         TreeNode<V> tr = t.right;
         TreeNode<V> tb = t.prev;
         TreeNode<V> tn = (TreeNode)t.next;
         if (tb != null && tb.next != t) {
            return false;
         } else if (tn != null && tn.prev != t) {
            return false;
         } else if (tp != null && t != tp.left && t != tp.right) {
            return false;
         } else if (tl == null || tl.parent == t && tl.hash <= t.hash) {
            if (tr == null || tr.parent == t && tr.hash >= t.hash) {
               if (t.red && tl != null && tl.red && tr != null && tr.red) {
                  return false;
               } else if (tl != null && !this.checkInvariants(tl)) {
                  return false;
               } else {
                  return tr == null || this.checkInvariants(tr);
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      static {
         try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe)f.get((Object)null);
            Class<?> k = TreeBin.class;
            LOCKSTATE = U.objectFieldOffset(k.getDeclaredField("lockState"));
         } catch (Exception e) {
            throw new Error(e);
         }
      }
   }

   protected static final class TableStack<V> {
      public int length;
      public int index;
      public Node<V>[] tab;
      public TableStack<V> next;

      public TableStack() {
      }
   }

   protected static class Traverser<V> {
      public Node<V>[] tab;
      public Node<V> next;
      public TableStack<V> stack;
      public TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;

      public Traverser(Node<V>[] tab, int size, int index, int limit) {
         this.tab = tab;
         this.baseSize = size;
         this.baseIndex = this.index = index;
         this.baseLimit = limit;
         this.next = null;
      }

      protected final Node<V> advance() {
         Node<V> e;
         if ((e = this.next) != null) {
            e = e.next;
         }

         while(e == null) {
            Node<V>[] t;
            int i;
            int n;
            if (this.baseIndex >= this.baseLimit || (t = this.tab) == null || (n = t.length) <= (i = this.index) || i < 0) {
               return this.next = null;
            }

            if ((e = Byte2ObjectConcurrentHashMap.<V>tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof ForwardingNode) {
                  this.tab = ((ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof TreeBin) {
                  e = ((TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (this.stack != null) {
               this.recoverState(n);
            } else if ((this.index = i + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }
         }

         return this.next = e;
      }

      protected void pushState(Node<V>[] t, int i, int n) {
         TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new TableStack<V>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         TableStack<V> s;
         int len;
         while((s = this.stack) != null && (this.index += len = s.length) >= n) {
            n = len;
            this.index = s.index;
            this.tab = s.tab;
            s.tab = null;
            TableStack<V> next = s.next;
            s.next = this.spare;
            this.stack = next;
            this.spare = s;
         }

         if (s == null && (this.index += this.baseSize) >= n) {
            this.index = ++this.baseIndex;
         }

      }
   }

   protected static class BaseIterator<V> extends Traverser<V> {
      public final Byte2ObjectConcurrentHashMap<V> map;
      public Node<V> lastReturned;

      public BaseIterator(Node<V>[] tab, int size, int index, int limit, Byte2ObjectConcurrentHashMap<V> map) {
         super(tab, size, index, limit);
         this.map = map;
         this.advance();
      }

      public final boolean hasNext() {
         return this.next != null;
      }

      public final boolean hasMoreElements() {
         return this.next != null;
      }

      public final void remove() {
         Node<V> p;
         if ((p = this.lastReturned) == null) {
            throw new IllegalStateException();
         } else {
            this.lastReturned = null;
            this.map.replaceNode(p.key, (Object)null, (Object)null);
         }
      }
   }

   protected static final class KeyIterator<V> implements ByteIterator {
      public Node<V>[] tab;
      public Node<V> next;
      public TableStack<V> stack;
      public TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;
      public final Byte2ObjectConcurrentHashMap<V> map;
      public Node<V> lastReturned;

      public KeyIterator(Node<V>[] tab, int size, int index, int limit, Byte2ObjectConcurrentHashMap<V> map) {
         this.tab = tab;
         this.baseSize = size;
         this.baseIndex = this.index = index;
         this.baseLimit = limit;
         this.next = null;
         this.map = map;
         this.advance();
      }

      protected final Node<V> advance() {
         Node<V> e;
         if ((e = this.next) != null) {
            e = e.next;
         }

         while(e == null) {
            Node<V>[] t;
            int i;
            int n;
            if (this.baseIndex >= this.baseLimit || (t = this.tab) == null || (n = t.length) <= (i = this.index) || i < 0) {
               return this.next = null;
            }

            if ((e = Byte2ObjectConcurrentHashMap.<V>tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof ForwardingNode) {
                  this.tab = ((ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof TreeBin) {
                  e = ((TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (this.stack != null) {
               this.recoverState(n);
            } else if ((this.index = i + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }
         }

         return this.next = e;
      }

      protected void pushState(Node<V>[] t, int i, int n) {
         TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new TableStack<V>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         TableStack<V> s;
         int len;
         while((s = this.stack) != null && (this.index += len = s.length) >= n) {
            n = len;
            this.index = s.index;
            this.tab = s.tab;
            s.tab = null;
            TableStack<V> next = s.next;
            s.next = this.spare;
            this.stack = next;
            this.spare = s;
         }

         if (s == null && (this.index += this.baseSize) >= n) {
            this.index = ++this.baseIndex;
         }

      }

      public final boolean hasNext() {
         return this.next != null;
      }

      public final boolean hasMoreElements() {
         return this.next != null;
      }

      public final void remove() {
         Node<V> p;
         if ((p = this.lastReturned) == null) {
            throw new IllegalStateException();
         } else {
            this.lastReturned = null;
            this.map.replaceNode(p.key, (Object)null, (Object)null);
         }
      }

      public final byte nextByte() {
         Node<V> p;
         if ((p = this.next) == null) {
            throw new NoSuchElementException();
         } else {
            byte k = p.key;
            this.lastReturned = p;
            this.advance();
            return k;
         }
      }
   }

   protected static final class ValueIterator<V> extends BaseIterator<V> implements ObjectIterator<V>, Enumeration<V> {
      public ValueIterator(Node<V>[] tab, int index, int size, int limit, Byte2ObjectConcurrentHashMap<V> map) {
         super(tab, index, size, limit, map);
      }

      public final V next() {
         Node<V> p;
         if ((p = this.next) == null) {
            throw new NoSuchElementException();
         } else {
            V v = p.val;
            this.lastReturned = p;
            this.advance();
            return v;
         }
      }

      public final V nextElement() {
         return (V)this.next();
      }
   }

   protected static final class EntryIterator<V> extends BaseIterator<V> implements ObjectIterator<Byte2ObjectMap.Entry<V>> {
      public EntryIterator(Node<V>[] tab, int index, int size, int limit, Byte2ObjectConcurrentHashMap<V> map) {
         super(tab, index, size, limit, map);
      }

      public final Entry<V> next() {
         Node<V> p;
         if ((p = this.next) == null) {
            throw new NoSuchElementException();
         } else {
            byte k = p.key;
            V v = p.val;
            this.lastReturned = p;
            this.advance();
            return new MapEntry<V>(p.isEmpty(), k, v, this.map);
         }
      }
   }

   public interface Entry<V> extends Byte2ObjectMap.Entry<V> {
      boolean isEmpty();

      /** @deprecated */
      @Deprecated
      Byte getKey();

      byte getByteKey();

      V getValue();

      int hashCode();

      String toString();

      boolean equals(Object var1);

      V setValue(V var1);
   }

   protected static final class MapEntry<V> implements Entry<V> {
      public final boolean empty;
      public final byte key;
      public V val;
      public final Byte2ObjectConcurrentHashMap<V> map;

      public MapEntry(boolean empty, byte key, V val, Byte2ObjectConcurrentHashMap<V> map) {
         this.empty = empty;
         this.key = key;
         this.val = val;
         this.map = map;
      }

      public boolean isEmpty() {
         return this.empty;
      }

      public Byte getKey() {
         return this.key;
      }

      public byte getByteKey() {
         return this.key;
      }

      public V getValue() {
         return this.val;
      }

      public String toString() {
         if (this.empty) {
            return "EMPTY=" + String.valueOf(this.val);
         } else {
            byte var10000 = this.key;
            return var10000 + "=" + String.valueOf(this.val);
         }
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o instanceof Entry) {
            if (this.empty != ((Entry)o).isEmpty()) {
               return false;
            } else if (!this.empty && this.key != ((Entry)o).getByteKey()) {
               return false;
            } else {
               return this.val.equals(((Entry)o).getValue());
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.empty ? 1 : 0;
         result = 31 * result + Byte.hashCode(this.key);
         result = 31 * result + this.val.hashCode();
         return result;
      }

      public V setValue(V value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            V v = this.val;
            this.val = value;
            this.map.put(this.key, value);
            return v;
         }
      }
   }

   protected static final class KeySpliterator<V> extends Traverser<V> implements ByteSpliterator {
      public long est;

      public KeySpliterator(Node<V>[] tab, int size, int index, int limit, long est) {
         super(tab, size, index, limit);
         this.est = est;
      }

      public ByteSpliterator trySplit() {
         int i;
         int f;
         int h;
         return (h = (i = this.baseIndex) + (f = this.baseLimit) >>> 1) <= i ? null : new KeySpliterator(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
      }

      public boolean tryAdvance(Consumer<? super Byte> action) {
         return action instanceof ByteConsumer ? this.tryAdvance((ByteConsumer)action) : this.tryAdvance((ByteConsumer)((value) -> action.accept(value)));
      }

      public void forEachRemaining(ByteConsumer action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V> p;
            while((p = this.advance()) != null) {
               action.accept(p.key);
            }

         }
      }

      public boolean tryAdvance(ByteConsumer action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(p.key);
               return true;
            }
         }
      }

      public long estimateSize() {
         return this.est;
      }

      public int characteristics() {
         return 4353;
      }
   }

   protected static final class ValueSpliterator<V> extends Traverser<V> implements ObjectSpliterator<V> {
      public long est;

      public ValueSpliterator(Node<V>[] tab, int size, int index, int limit, long est) {
         super(tab, size, index, limit);
         this.est = est;
      }

      public ObjectSpliterator<V> trySplit() {
         int i;
         int f;
         int h;
         return (h = (i = this.baseIndex) + (f = this.baseLimit) >>> 1) <= i ? null : new ValueSpliterator(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1);
      }

      public void forEachRemaining(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V> p;
            while((p = this.advance()) != null) {
               action.accept(p.val);
            }

         }
      }

      public boolean tryAdvance(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(p.val);
               return true;
            }
         }
      }

      public long estimateSize() {
         return this.est;
      }

      public int characteristics() {
         return 4352;
      }
   }

   protected static final class EntrySpliterator<V> extends Traverser<V> implements ObjectSpliterator<Byte2ObjectMap.Entry<V>> {
      public final Byte2ObjectConcurrentHashMap<V> map;
      public long est;

      public EntrySpliterator(Node<V>[] tab, int size, int index, int limit, long est, Byte2ObjectConcurrentHashMap<V> map) {
         super(tab, size, index, limit);
         this.map = map;
         this.est = est;
      }

      public ObjectSpliterator<Byte2ObjectMap.Entry<V>> trySplit() {
         int i;
         int f;
         int h;
         return (h = (i = this.baseIndex) + (f = this.baseLimit) >>> 1) <= i ? null : new EntrySpliterator(this.tab, this.baseSize, this.baseLimit = h, f, this.est >>>= 1, this.map);
      }

      public void forEachRemaining(Consumer<? super Byte2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V> p;
            while((p = this.advance()) != null) {
               action.accept(new MapEntry(p.isEmpty(), p.key, p.val, this.map));
            }

         }
      }

      public boolean tryAdvance(Consumer<? super Byte2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V> p;
            if ((p = this.advance()) == null) {
               return false;
            } else {
               action.accept(new MapEntry(p.isEmpty(), p.key, p.val, this.map));
               return true;
            }
         }
      }

      public long estimateSize() {
         return this.est;
      }

      public int characteristics() {
         return 4353;
      }
   }

   protected abstract static class CollectionView<K, E> implements ObjectCollection<E>, Serializable {
      public static final long serialVersionUID = 7249069246763182397L;
      public final Byte2ObjectConcurrentHashMap<K> map;
      protected static final String oomeMsg = "Required array size too large";

      public CollectionView(Byte2ObjectConcurrentHashMap<K> map) {
         this.map = map;
      }

      public Byte2ObjectConcurrentHashMap<K> getMap() {
         return this.map;
      }

      public final void clear() {
         this.map.clear();
      }

      public final int size() {
         return this.map.size();
      }

      public final boolean isEmpty() {
         return this.map.isEmpty();
      }

      public abstract ObjectIterator<E> iterator();

      public abstract boolean contains(Object var1);

      public abstract boolean remove(Object var1);

      public final Object[] toArray() {
         long sz = this.map.mappingCount();
         if (sz > 2147483639L) {
            throw new OutOfMemoryError("Required array size too large");
         } else {
            int n = (int)sz;
            Object[] r = new Object[n];
            int i = 0;

            E e;
            for(ObjectIterator var6 = this.iterator(); var6.hasNext(); r[i++] = e) {
               e = (E)var6.next();
               if (i == n) {
                  if (n >= 2147483639) {
                     throw new OutOfMemoryError("Required array size too large");
                  }

                  if (n >= 1073741819) {
                     n = 2147483639;
                  } else {
                     n += (n >>> 1) + 1;
                  }

                  r = Arrays.copyOf(r, n);
               }
            }

            return i == n ? r : Arrays.copyOf(r, i);
         }
      }

      public final <T> T[] toArray(T[] a) {
         long sz = this.map.mappingCount();
         if (sz > 2147483639L) {
            throw new OutOfMemoryError("Required array size too large");
         } else {
            int m = (int)sz;
            T[] r = (T[])(a.length >= m ? a : (Object[])Array.newInstance(a.getClass().getComponentType(), m));
            int n = r.length;
            int i = 0;

            E e;
            for(ObjectIterator var8 = this.iterator(); var8.hasNext(); r[i++] = e) {
               e = (E)var8.next();
               if (i == n) {
                  if (n >= 2147483639) {
                     throw new OutOfMemoryError("Required array size too large");
                  }

                  if (n >= 1073741819) {
                     n = 2147483639;
                  } else {
                     n += (n >>> 1) + 1;
                  }

                  r = (T[])Arrays.copyOf(r, n);
               }
            }

            if (a == r && i < n) {
               r[i] = null;
               return r;
            } else {
               return (T[])(i == n ? r : Arrays.copyOf(r, i));
            }
         }
      }

      public final String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append('[');
         Iterator<E> it = this.iterator();
         if (it.hasNext()) {
            while(true) {
               Object e = it.next();
               sb.append(e == this ? "(this Collection)" : e);
               if (!it.hasNext()) {
                  break;
               }

               sb.append(',').append(' ');
            }
         }

         return sb.append(']').toString();
      }

      public final boolean containsAll(Collection<?> c) {
         if (c != this) {
            for(Object e : c) {
               if (e == null || !this.contains(e)) {
                  return false;
               }
            }
         }

         return true;
      }

      public final boolean removeAll(Collection<?> c) {
         if (c == null) {
            throw new NullPointerException();
         } else {
            boolean modified = false;
            Iterator<E> it = this.iterator();

            while(it.hasNext()) {
               if (c.contains(it.next())) {
                  it.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }

      public final boolean retainAll(Collection<?> c) {
         if (c == null) {
            throw new NullPointerException();
         } else {
            boolean modified = false;
            Iterator<E> it = this.iterator();

            while(it.hasNext()) {
               if (!c.contains(it.next())) {
                  it.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }
   }

   public static class KeySetView<V> implements ByteSet {
      public static final long serialVersionUID = 7249069246763182397L;
      public final Byte2ObjectConcurrentHashMap<V> map;
      public final V value;

      public KeySetView(Byte2ObjectConcurrentHashMap<V> map, V value) {
         this.map = map;
         this.value = value;
      }

      public V getMappedValue() {
         return this.value;
      }

      public boolean contains(byte o) {
         return this.map.containsKey(o);
      }

      public boolean remove(byte o) {
         return this.map.remove(o) != null;
      }

      public ByteIterator iterator() {
         Byte2ObjectConcurrentHashMap<V> m = this.map;
         Node<V>[] t;
         int f = (t = m.table) == null ? 0 : t.length;
         return new KeyIterator(t, f, 0, f, m);
      }

      public boolean add(byte e) {
         V v;
         if ((v = this.value) == null) {
            throw new UnsupportedOperationException();
         } else {
            return this.map.putVal(e, v, true) == null;
         }
      }

      public boolean addAll(ByteCollection c) {
         boolean added = false;
         V v;
         if ((v = this.value) == null) {
            throw new UnsupportedOperationException();
         } else {
            ByteIterator iter = c.iterator();

            while(iter.hasNext()) {
               byte e = iter.nextByte();
               if (this.map.putVal(e, v, true) == null) {
                  added = true;
               }
            }

            return added;
         }
      }

      public int hashCode() {
         int h = 0;

         for(ByteIterator iter = this.iterator(); iter.hasNext(); h += Byte.hashCode(iter.nextByte())) {
         }

         return h;
      }

      public boolean equals(Object o) {
         ByteSet c;
         return o instanceof ByteSet && ((c = (ByteSet)o) == this || this.containsAll(c) && c.containsAll(this));
      }

      public byte getNoEntryValue() {
         return this.map.EMPTY;
      }

      public int size() {
         return this.map.size();
      }

      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      public Object[] toArray() {
         Object[] out = new Byte[this.size()];
         ByteIterator iter = this.iterator();

         int i;
         for(i = 0; i < out.length && iter.hasNext(); ++i) {
            out[i] = iter.nextByte();
         }

         if (out.length > i + 1) {
            out[i] = this.map.EMPTY;
         }

         return out;
      }

      public Object[] toArray(Object[] dest) {
         ByteIterator iter = this.iterator();

         int i;
         for(i = 0; i < dest.length && iter.hasNext() && i <= dest.length; ++i) {
            dest[i] = iter.next();
         }

         if (dest.length > i + 1) {
            dest[i] = this.map.EMPTY;
         }

         return dest;
      }

      public byte[] toByteArray() {
         byte[] out = new byte[this.size()];
         ByteIterator iter = this.iterator();

         int i;
         for(i = 0; i < out.length && iter.hasNext(); ++i) {
            out[i] = iter.next();
         }

         if (out.length > i + 1) {
            out[i] = this.map.EMPTY;
         }

         return out;
      }

      public byte[] toArray(byte[] dest) {
         ByteIterator iter = this.iterator();

         int i;
         for(i = 0; i < dest.length && iter.hasNext() && i <= dest.length; ++i) {
            dest[i] = iter.next();
         }

         if (dest.length > i + 1) {
            dest[i] = this.map.EMPTY;
         }

         return dest;
      }

      public byte[] toByteArray(byte[] dest) {
         return this.toArray(dest);
      }

      public boolean containsAll(Collection<?> collection) {
         for(Object element : collection) {
            if (!(element instanceof Long)) {
               return false;
            }

            byte c = (Byte)element;
            if (!this.contains(c)) {
               return false;
            }
         }

         return true;
      }

      public boolean containsAll(ByteCollection collection) {
         ByteIterator iter = collection.iterator();

         while(iter.hasNext()) {
            byte element = iter.next();
            if (!this.contains(element)) {
               return false;
            }
         }

         return true;
      }

      public boolean containsAll(byte[] array) {
         int i = array.length;

         while(i-- > 0) {
            if (!this.contains(array[i])) {
               return false;
            }
         }

         return true;
      }

      public boolean addAll(Collection<? extends Byte> collection) {
         boolean changed = false;

         for(Byte element : collection) {
            byte e = element;
            if (this.add(e)) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean addAll(byte[] array) {
         boolean changed = false;
         int i = array.length;

         while(i-- > 0) {
            if (this.add(array[i])) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;
         ByteIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (!collection.contains(iter.next())) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      public boolean retainAll(ByteCollection collection) {
         if (this == collection) {
            return false;
         } else {
            boolean modified = false;
            ByteIterator iter = this.iterator();

            while(iter.hasNext()) {
               if (!collection.contains(iter.next())) {
                  iter.remove();
                  modified = true;
               }
            }

            return modified;
         }
      }

      public boolean retainAll(byte[] array) {
         boolean modified = false;
         ByteIterator iter = this.iterator();

         while(iter.hasNext()) {
            if (Arrays.binarySearch(array, iter.next()) < 0) {
               iter.remove();
               modified = true;
            }
         }

         return modified;
      }

      public boolean removeAll(Collection<?> collection) {
         boolean changed = false;

         for(Object element : collection) {
            if (element instanceof Byte) {
               byte c = (Byte)element;
               if (this.remove(c)) {
                  changed = true;
               }
            }
         }

         return changed;
      }

      public boolean removeAll(ByteCollection collection) {
         boolean changed = false;
         ByteIterator iter = collection.iterator();

         while(iter.hasNext()) {
            byte element = iter.next();
            if (this.remove(element)) {
               changed = true;
            }
         }

         return changed;
      }

      public boolean removeAll(byte[] array) {
         boolean changed = false;
         int i = array.length;

         while(i-- > 0) {
            if (this.remove(array[i])) {
               changed = true;
            }
         }

         return changed;
      }

      public void clear() {
         this.map.clear();
      }

      public ByteSpliterator spliterator() {
         Byte2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Node<V>[] t;
         int f = (t = m.table) == null ? 0 : t.length;
         return new KeySpliterator(t, f, 0, f, n < 0L ? 0L : n);
      }
   }

   protected static final class ValuesView<V> extends CollectionView<V, V> implements FastCollection<V>, Serializable {
      public static final long serialVersionUID = 2249069246763182397L;

      public ValuesView(Byte2ObjectConcurrentHashMap<V> map) {
         super(map);
      }

      public final boolean contains(Object o) {
         return this.map.containsValue(o);
      }

      public final boolean remove(Object o) {
         if (o != null) {
            Iterator<V> it = this.iterator();

            while(it.hasNext()) {
               if (o.equals(it.next())) {
                  it.remove();
                  return true;
               }
            }
         }

         return false;
      }

      public final ObjectIterator<V> iterator() {
         Byte2ObjectConcurrentHashMap<V> m = this.map;
         Node<V>[] t;
         int f = (t = m.table) == null ? 0 : t.length;
         return new ValueIterator<V>(t, f, 0, f, m);
      }

      public final boolean add(V e) {
         throw new UnsupportedOperationException();
      }

      public final boolean addAll(Collection<? extends V> c) {
         throw new UnsupportedOperationException();
      }

      public ObjectSpliterator<V> spliterator() {
         Byte2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Node<V>[] t;
         int f = (t = m.table) == null ? 0 : t.length;
         return new ValueSpliterator<V>(t, f, 0, f, n < 0L ? 0L : n);
      }

      public void forEach(Consumer<? super V> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] tt;
            if ((tt = this.map.table) != null) {
               Node<V>[] tab = tt;
               Node<V> next = null;
               TableStack<V> stack = null;
               TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while(true) {
                  Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label80: {
                     while(true) {
                        if (p != null) {
                           next = p;
                           break label80;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                        } else {
                           while(true) {
                              TableStack<V> s = stack;
                              int len;
                              if (stack == null || (index += len = stack.length) < n) {
                                 if (stack == null && (index += baseSize) >= n) {
                                    ++baseIndex;
                                    index = baseIndex;
                                 }
                                 break;
                              }

                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  action.accept(p.val);
               }
            }

         }
      }

      public <A, B, C, D> void forEach(FastCollection.FastConsumerD9<? super V, A, B, C, D> consumer, A a, double d1, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, B b, C c, D d) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] tt;
            if ((tt = this.map.table) != null) {
               Node<V>[] tab = tt;
               Node<V> next = null;
               TableStack<V> stack = null;
               TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while(true) {
                  Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label80: {
                     while(true) {
                        if (p != null) {
                           next = p;
                           break label80;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                        } else {
                           while(true) {
                              TableStack<V> s = stack;
                              int len;
                              if (stack == null || (index += len = stack.length) < n) {
                                 if (stack == null && (index += baseSize) >= n) {
                                    ++baseIndex;
                                    index = baseIndex;
                                 }
                                 break;
                              }

                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, a, d1, d2, d3, d4, d5, d6, d7, d8, d9, b, c, d);
               }
            }

         }
      }

      public <A, B, C, D> void forEach(FastCollection.FastConsumerD6<? super V, A, B, C, D> consumer, A a, double d1, double d2, double d3, double d4, double d5, double d6, B b, C c, D d) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] tt;
            if ((tt = this.map.table) != null) {
               Node<V>[] tab = tt;
               Node<V> next = null;
               TableStack<V> stack = null;
               TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while(true) {
                  Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label80: {
                     while(true) {
                        if (p != null) {
                           next = p;
                           break label80;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                        } else {
                           while(true) {
                              TableStack<V> s = stack;
                              int len;
                              if (stack == null || (index += len = stack.length) < n) {
                                 if (stack == null && (index += baseSize) >= n) {
                                    ++baseIndex;
                                    index = baseIndex;
                                 }
                                 break;
                              }

                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, a, d1, d2, d3, d4, d5, d6, b, c, d);
               }
            }

         }
      }

      public void forEachWithFloat(FastCollection.FastConsumerF<? super V> consumer, float ii) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] tt;
            if ((tt = this.map.table) != null) {
               Node<V>[] tab = tt;
               Node<V> next = null;
               TableStack<V> stack = null;
               TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while(true) {
                  Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label80: {
                     while(true) {
                        if (p != null) {
                           next = p;
                           break label80;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                        } else {
                           while(true) {
                              TableStack<V> s = stack;
                              int len;
                              if (stack == null || (index += len = stack.length) < n) {
                                 if (stack == null && (index += baseSize) >= n) {
                                    ++baseIndex;
                                    index = baseIndex;
                                 }
                                 break;
                              }

                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, ii);
               }
            }

         }
      }

      public void forEachWithInt(FastCollection.FastConsumerI<? super V> consumer, int ii) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] tt;
            if ((tt = this.map.table) != null) {
               Node<V>[] tab = tt;
               Node<V> next = null;
               TableStack<V> stack = null;
               TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while(true) {
                  Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label80: {
                     while(true) {
                        if (p != null) {
                           next = p;
                           break label80;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                        } else {
                           while(true) {
                              TableStack<V> s = stack;
                              int len;
                              if (stack == null || (index += len = stack.length) < n) {
                                 if (stack == null && (index += baseSize) >= n) {
                                    ++baseIndex;
                                    index = baseIndex;
                                 }
                                 break;
                              }

                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, ii);
               }
            }

         }
      }

      public void forEachWithLong(FastCollection.FastConsumerL<? super V> consumer, long ii) {
         if (consumer == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] tt;
            if ((tt = this.map.table) != null) {
               Node<V>[] tab = tt;
               Node<V> next = null;
               TableStack<V> stack = null;
               TableStack<V> spare = null;
               int index = 0;
               int baseIndex = 0;
               int baseLimit = tt.length;
               int baseSize = tt.length;

               while(true) {
                  Node<V> p = null;
                  p = next;
                  if (next != null) {
                     p = next.next;
                  }

                  label80: {
                     while(true) {
                        if (p != null) {
                           next = p;
                           break label80;
                        }

                        if (baseIndex >= baseLimit) {
                           break;
                        }

                        Node<V>[] t = tab;
                        int n;
                        if (tab == null || (n = tab.length) <= index || index < 0) {
                           break;
                        }

                        if ((p = Byte2ObjectConcurrentHashMap.<V>tabAt(tab, index)) != null && p.hash < 0) {
                           if (p instanceof ForwardingNode) {
                              tab = ((ForwardingNode)p).nextTable;
                              p = null;
                              TableStack<V> s = spare;
                              if (spare != null) {
                                 spare = spare.next;
                              } else {
                                 s = new TableStack<V>();
                              }

                              s.tab = t;
                              s.length = n;
                              s.index = index;
                              s.next = stack;
                              stack = s;
                              continue;
                           }

                           if (p instanceof TreeBin) {
                              p = ((TreeBin)p).first;
                           } else {
                              p = null;
                           }
                        }

                        if (stack == null) {
                           if ((index += baseSize) >= n) {
                              ++baseIndex;
                              index = baseIndex;
                           }
                        } else {
                           while(true) {
                              TableStack<V> s = stack;
                              int len;
                              if (stack == null || (index += len = stack.length) < n) {
                                 if (stack == null && (index += baseSize) >= n) {
                                    ++baseIndex;
                                    index = baseIndex;
                                 }
                                 break;
                              }

                              n = len;
                              index = stack.index;
                              tab = stack.tab;
                              stack.tab = null;
                              TableStack<V> anext = stack.next;
                              stack.next = spare;
                              stack = anext;
                              spare = s;
                           }
                        }
                     }

                     next = null;
                  }

                  if (p == null) {
                     break;
                  }

                  consumer.accept(p.val, ii);
               }
            }

         }
      }
   }

   protected static final class EntrySetView<V> extends CollectionView<V, Byte2ObjectMap.Entry<V>> implements ObjectSet<Byte2ObjectMap.Entry<V>>, Serializable {
      public static final long serialVersionUID = 2249069246763182397L;

      public EntrySetView(Byte2ObjectConcurrentHashMap<V> map) {
         super(map);
      }

      public boolean contains(Object o) {
         if (o instanceof Byte2ObjectMap.Entry) {
            Byte2ObjectMap.Entry<?> e;
            byte k = (e = (Byte2ObjectMap.Entry)o).getByteKey();
            if (!((Entry)o).isEmpty()) {
               Object v;
               Object r;
               return (r = this.map.get(k)) != null && (v = e.getValue()) != null && (v == r || v.equals(r));
            }
         }

         return false;
      }

      public boolean remove(Object o) {
         if (o instanceof Byte2ObjectMap.Entry) {
            Byte2ObjectMap.Entry<?> e;
            byte k = (e = (Byte2ObjectMap.Entry)o).getByteKey();
            if (!((Entry)o).isEmpty()) {
               Object v;
               return (v = e.getValue()) != null && this.map.remove(k, v);
            }
         }

         return false;
      }

      public ObjectIterator<Byte2ObjectMap.Entry<V>> iterator() {
         Byte2ObjectConcurrentHashMap<V> m = this.map;
         Node<V>[] t;
         int f = (t = m.table) == null ? 0 : t.length;
         return new EntryIterator<Byte2ObjectMap.Entry<V>>(t, f, 0, f, m);
      }

      public boolean add(Byte2ObjectMap.Entry<V> e) {
         return this.map.putVal(e.getByteKey(), e.getValue(), false) == null;
      }

      public boolean addAll(Collection<? extends Byte2ObjectMap.Entry<V>> c) {
         boolean added = false;

         for(Byte2ObjectMap.Entry<V> e : c) {
            if (this.add(e)) {
               added = true;
            }
         }

         return added;
      }

      public final int hashCode() {
         int h = 0;
         Node<V>[] t;
         Node<V> p;
         if ((t = this.map.table) != null) {
            for(Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length); (p = it.advance()) != null; h += p.hashCode()) {
            }
         }

         return h;
      }

      public final boolean equals(Object o) {
         Set<?> c;
         return o instanceof Set && ((c = (Set)o) == this || this.containsAll(c) && c.containsAll(this));
      }

      public ObjectSpliterator<Byte2ObjectMap.Entry<V>> spliterator() {
         Byte2ObjectConcurrentHashMap<V> m = this.map;
         long n = m.sumCount();
         Node<V>[] t;
         int f = (t = m.table) == null ? 0 : t.length;
         return new EntrySpliterator<Byte2ObjectMap.Entry<V>>(t, f, 0, f, n < 0L ? 0L : n, m);
      }

      public void forEach(Consumer<? super Byte2ObjectMap.Entry<V>> action) {
         if (action == null) {
            throw new NullPointerException();
         } else {
            Node<V>[] t;
            if ((t = this.map.table) != null) {
               Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length);

               Node<V> p;
               while((p = it.advance()) != null) {
                  action.accept(new MapEntry(p.isEmpty(), p.key, p.val, this.map));
               }
            }

         }
      }
   }

   protected abstract static class BulkTask<V, R> extends CountedCompleter<R> {
      public Node<V>[] tab;
      public Node<V> next;
      public TableStack<V> stack;
      public TableStack<V> spare;
      public int index;
      public int baseIndex;
      public int baseLimit;
      public final int baseSize;
      public int batch;

      protected BulkTask(BulkTask<V, ?> par, int b, int i, int f, Node<V>[] t) {
         super(par);
         this.batch = b;
         this.index = this.baseIndex = i;
         if ((this.tab = t) == null) {
            this.baseSize = this.baseLimit = 0;
         } else if (par == null) {
            this.baseSize = this.baseLimit = t.length;
         } else {
            this.baseLimit = f;
            this.baseSize = par.baseSize;
         }

      }

      protected final Node<V> advance() {
         Node<V> e;
         if ((e = this.next) != null) {
            e = e.next;
         }

         while(e == null) {
            Node<V>[] t;
            int i;
            int n;
            if (this.baseIndex >= this.baseLimit || (t = this.tab) == null || (n = t.length) <= (i = this.index) || i < 0) {
               return this.next = null;
            }

            if ((e = Byte2ObjectConcurrentHashMap.<V>tabAt(t, i)) != null && e.hash < 0) {
               if (e instanceof ForwardingNode) {
                  this.tab = ((ForwardingNode)e).nextTable;
                  e = null;
                  this.pushState(t, i, n);
                  continue;
               }

               if (e instanceof TreeBin) {
                  e = ((TreeBin)e).first;
               } else {
                  e = null;
               }
            }

            if (this.stack != null) {
               this.recoverState(n);
            } else if ((this.index = i + this.baseSize) >= n) {
               this.index = ++this.baseIndex;
            }
         }

         return this.next = e;
      }

      protected void pushState(Node<V>[] t, int i, int n) {
         TableStack<V> s = this.spare;
         if (s != null) {
            this.spare = s.next;
         } else {
            s = new TableStack<V>();
         }

         s.tab = t;
         s.length = n;
         s.index = i;
         s.next = this.stack;
         this.stack = s;
      }

      protected void recoverState(int n) {
         TableStack<V> s;
         int len;
         while((s = this.stack) != null && (this.index += len = s.length) >= n) {
            n = len;
            this.index = s.index;
            this.tab = s.tab;
            s.tab = null;
            TableStack<V> next = s.next;
            s.next = this.spare;
            this.stack = next;
            this.spare = s;
         }

         if (s == null && (this.index += this.baseSize) >= n) {
            this.index = ++this.baseIndex;
         }

      }
   }

   protected abstract static class ByteReturningBulkTask2<V> extends BulkTask<V, Byte> {
      public byte result;

      public ByteReturningBulkTask2(BulkTask<V, ?> par, int b, int i, int f, Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected byte invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   protected abstract static class LongReturningBulkTask<V> extends BulkTask<V, Long> {
      public long result;

      public LongReturningBulkTask(BulkTask<V, ?> par, int b, int i, int f, Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected long invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   protected abstract static class IntReturningBulkTask<V> extends BulkTask<V, Integer> {
      public int result;

      public IntReturningBulkTask(BulkTask<V, ?> par, int b, int i, int f, Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected int invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   protected abstract static class DoubleReturningBulkTask<V> extends BulkTask<V, Double> {
      public double result;

      public DoubleReturningBulkTask(BulkTask<V, ?> par, int b, int i, int f, Node<V>[] t) {
         super(par, b, i, f, t);
      }

      protected double invoke0() {
         this.quietlyInvoke();
         Throwable exc = this.getException();
         if (exc != null) {
            throw SneakyThrow.sneakyThrow(exc);
         } else {
            return this.result;
         }
      }
   }

   protected static final class ForEachKeyTask<V> extends BulkTask<V, Void> {
      public final ByteConsumer action;

      public ForEachKeyTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ByteConsumer action) {
         super(p, b, i, f, t);
         this.action = action;
      }

      public final void compute() {
         ByteConsumer action;
         if ((action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachKeyTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action)).fork();
            }

            while((p = this.advance()) != null) {
               action.accept(p.key);
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachValueTask<V> extends BulkTask<V, Void> {
      public final Consumer<? super V> action;

      public ForEachValueTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, Consumer<? super V> action) {
         super(p, b, i, f, t);
         this.action = action;
      }

      public final void compute() {
         Consumer<? super V> action;
         if ((action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachValueTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action)).fork();
            }

            while((p = this.advance()) != null) {
               action.accept(p.val);
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachEntryTask<V> extends BulkTask<V, Void> {
      public final Consumer<? super Entry<V>> action;

      public ForEachEntryTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, Consumer<? super Entry<V>> action) {
         super(p, b, i, f, t);
         this.action = action;
      }

      public final void compute() {
         Consumer<? super Entry<V>> action;
         if ((action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachEntryTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action)).fork();
            }

            while((p = this.advance()) != null) {
               action.accept(p);
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachMappingTask<V> extends BulkTask<V, Void> {
      public final ByteObjConsumer<? super V> action;

      public ForEachMappingTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ByteObjConsumer<? super V> action) {
         super(p, b, i, f, t);
         this.action = action;
      }

      public final void compute() {
         ByteObjConsumer<? super V> action;
         if ((action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachMappingTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, action)).fork();
            }

            while((p = this.advance()) != null) {
               action.accept(p.key, p.val);
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachTransformedKeyTask<V, U> extends BulkTask<V, Void> {
      public final ByteFunction<? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedKeyTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ByteFunction<? extends U> transformer, Consumer<? super U> action) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      public final void compute() {
         ByteFunction<? extends U> transformer;
         Consumer<? super U> action;
         if ((transformer = this.transformer) != null && (action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachTransformedKeyTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)).fork();
            }

            while((p = this.advance()) != null) {
               U u;
               if ((u = transformer.apply(p.key)) != null) {
                  action.accept(u);
               }
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachTransformedValueTask<V, U> extends BulkTask<V, Void> {
      public final Function<? super V, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedValueTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      public final void compute() {
         Function<? super V, ? extends U> transformer;
         Consumer<? super U> action;
         if ((transformer = this.transformer) != null && (action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachTransformedValueTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)).fork();
            }

            while((p = this.advance()) != null) {
               U u;
               if ((u = (U)transformer.apply(p.val)) != null) {
                  action.accept(u);
               }
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachTransformedEntryTask<V, U> extends BulkTask<V, Void> {
      public final Function<Entry<V>, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedEntryTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, Function<Entry<V>, ? extends U> transformer, Consumer<? super U> action) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      public final void compute() {
         Function<Entry<V>, ? extends U> transformer;
         Consumer<? super U> action;
         if ((transformer = this.transformer) != null && (action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachTransformedEntryTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)).fork();
            }

            while((p = this.advance()) != null) {
               U u;
               if ((u = (U)transformer.apply(p)) != null) {
                  action.accept(u);
               }
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class ForEachTransformedMappingTask<V, U> extends BulkTask<V, Void> {
      public final ByteObjFunction<? super V, ? extends U> transformer;
      public final Consumer<? super U> action;

      public ForEachTransformedMappingTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ByteObjFunction<? super V, ? extends U> transformer, Consumer<? super U> action) {
         super(p, b, i, f, t);
         this.transformer = transformer;
         this.action = action;
      }

      public final void compute() {
         ByteObjFunction<? super V, ? extends U> transformer;
         Consumer<? super U> action;
         if ((transformer = this.transformer) != null && (action = this.action) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (new ForEachTransformedMappingTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, transformer, action)).fork();
            }

            while((p = this.advance()) != null) {
               U u;
               if ((u = transformer.apply(p.key, p.val)) != null) {
                  action.accept(u);
               }
            }

            this.propagateCompletion();
         }

      }
   }

   protected static final class SearchKeysTask<V, U> extends BulkTask<V, U> {
      public final ByteFunction<? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchKeysTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ByteFunction<? extends U> searchFunction, AtomicReference<U> result) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      public final U getRawResult() {
         return (U)this.result.get();
      }

      public final void compute() {
         ByteFunction<? extends U> searchFunction;
         AtomicReference<U> result;
         if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               if (result.get() != null) {
                  return;
               }

               this.addToPendingCount(1);
               (new SearchKeysTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result)).fork();
            }

            while(result.get() == null) {
               Node<V> p;
               if ((p = this.advance()) == null) {
                  this.propagateCompletion();
                  break;
               }

               U u;
               if ((u = searchFunction.apply(p.key)) != null) {
                  if (result.compareAndSet((Object)null, u)) {
                     this.quietlyCompleteRoot();
                  }
                  break;
               }
            }
         }

      }
   }

   protected static final class SearchValuesTask<V, U> extends BulkTask<V, U> {
      public final Function<? super V, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchValuesTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, Function<? super V, ? extends U> searchFunction, AtomicReference<U> result) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      public final U getRawResult() {
         return (U)this.result.get();
      }

      public final void compute() {
         Function<? super V, ? extends U> searchFunction;
         AtomicReference<U> result;
         if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               if (result.get() != null) {
                  return;
               }

               this.addToPendingCount(1);
               (new SearchValuesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result)).fork();
            }

            while(result.get() == null) {
               Node<V> p;
               if ((p = this.advance()) == null) {
                  this.propagateCompletion();
                  break;
               }

               U u;
               if ((u = (U)searchFunction.apply(p.val)) != null) {
                  if (result.compareAndSet((Object)null, u)) {
                     this.quietlyCompleteRoot();
                  }
                  break;
               }
            }
         }

      }
   }

   protected static final class SearchEntriesTask<V, U> extends BulkTask<V, U> {
      public final Function<Entry<V>, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchEntriesTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, Function<Entry<V>, ? extends U> searchFunction, AtomicReference<U> result) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      public final U getRawResult() {
         return (U)this.result.get();
      }

      public final void compute() {
         Function<Entry<V>, ? extends U> searchFunction;
         AtomicReference<U> result;
         if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               if (result.get() != null) {
                  return;
               }

               this.addToPendingCount(1);
               (new SearchEntriesTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result)).fork();
            }

            while(result.get() == null) {
               Node<V> p;
               if ((p = this.advance()) == null) {
                  this.propagateCompletion();
                  break;
               }

               U u;
               if ((u = (U)searchFunction.apply(p)) != null) {
                  if (result.compareAndSet((Object)null, u)) {
                     this.quietlyCompleteRoot();
                  }

                  return;
               }
            }
         }

      }
   }

   protected static final class SearchMappingsTask<V, U> extends BulkTask<V, U> {
      public final ByteObjFunction<? super V, ? extends U> searchFunction;
      public final AtomicReference<U> result;

      public SearchMappingsTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ByteObjFunction<? super V, ? extends U> searchFunction, AtomicReference<U> result) {
         super(p, b, i, f, t);
         this.searchFunction = searchFunction;
         this.result = result;
      }

      public final U getRawResult() {
         return (U)this.result.get();
      }

      public final void compute() {
         ByteObjFunction<? super V, ? extends U> searchFunction;
         AtomicReference<U> result;
         if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               if (result.get() != null) {
                  return;
               }

               this.addToPendingCount(1);
               (new SearchMappingsTask(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, searchFunction, result)).fork();
            }

            while(result.get() == null) {
               Node<V> p;
               if ((p = this.advance()) == null) {
                  this.propagateCompletion();
                  break;
               }

               U u;
               if ((u = searchFunction.apply(p.key, p.val)) != null) {
                  if (result.compareAndSet((Object)null, u)) {
                     this.quietlyCompleteRoot();
                  }
                  break;
               }
            }
         }

      }
   }

   protected static final class ReduceKeysTask<V> extends ByteReturningBulkTask2<V> {
      public final byte EMPTY;
      public final ByteReduceTaskOperator reducer;
      public ReduceKeysTask<V> rights;
      public ReduceKeysTask<V> nextRight;

      public ReduceKeysTask(byte EMPTY, BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ReduceKeysTask<V> nextRight, ByteReduceTaskOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.EMPTY = EMPTY;
         this.reducer = reducer;
      }

      public final Byte getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ByteReduceTaskOperator reducer;
         if ((reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new ReduceKeysTask<V>(this.EMPTY, this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer)).fork();
            }

            i = 0;
            f = this.EMPTY;

            while((p = this.advance()) != null) {
               byte u = p.key;
               if (!i) {
                  i = 1;
                  f = u;
               } else if (!p.isEmpty()) {
                  i = 1;
                  f = reducer.reduce(this.EMPTY, (byte)f, u);
               }
            }

            this.result = (byte)f;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               ReduceKeysTask<V> t = (ReduceKeysTask)c;

               for(ReduceKeysTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  byte sr;
                  if ((sr = s.result) != this.EMPTY) {
                     byte tr;
                     t.result = (tr = t.result) == this.EMPTY ? sr : reducer.reduce(this.EMPTY, tr, sr);
                  }
               }
            }
         }

      }
   }

   protected static final class ReduceValuesTask<V> extends BulkTask<V, V> {
      public final BiFunction<? super V, ? super V, ? extends V> reducer;
      public V result;
      public ReduceValuesTask<V> rights;
      public ReduceValuesTask<V> nextRight;

      public ReduceValuesTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ReduceValuesTask<V> nextRight, BiFunction<? super V, ? super V, ? extends V> reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.reducer = reducer;
      }

      public final V getRawResult() {
         return this.result;
      }

      public final void compute() {
         BiFunction<? super V, ? super V, ? extends V> reducer;
         if ((reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new ReduceValuesTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer)).fork();
            }

            for(r = null; (p = this.advance()) != null; r = (V)(r == null ? v : reducer.apply(r, v))) {
               v = p.val;
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               ReduceValuesTask<V> t = (ReduceValuesTask)c;

               for(ReduceValuesTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  V sr;
                  if ((sr = s.result) != null) {
                     V tr;
                     t.result = (V)((tr = t.result) == null ? sr : reducer.apply(tr, sr));
                  }
               }
            }
         }

      }
   }

   protected static final class ReduceEntriesTask<V> extends BulkTask<V, Entry<V>> {
      public final BiFunction<Entry<V>, Entry<V>, ? extends Entry<V>> reducer;
      public Entry<V> result;
      public ReduceEntriesTask<V> rights;
      public ReduceEntriesTask<V> nextRight;

      public ReduceEntriesTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, ReduceEntriesTask<V> nextRight, BiFunction<Entry<V>, Entry<V>, ? extends Entry<V>> reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.reducer = reducer;
      }

      public final Entry<V> getRawResult() {
         return this.result;
      }

      public final void compute() {
         BiFunction<Entry<V>, Entry<V>, ? extends Entry<V>> reducer;
         if ((reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new ReduceEntriesTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, reducer)).fork();
            }

            for(r = null; (p = this.advance()) != null; r = (Entry<V>)(r == null ? p : (Entry)reducer.apply(r, p))) {
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               ReduceEntriesTask<V> t = (ReduceEntriesTask)c;

               for(ReduceEntriesTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  Entry<V> sr;
                  if ((sr = s.result) != null) {
                     Entry<V> tr;
                     t.result = (tr = t.result) == null ? sr : (Entry)reducer.apply(tr, sr);
                  }
               }
            }
         }

      }
   }

   protected static final class MapReduceKeysTask<V, U> extends BulkTask<V, U> {
      public final ByteFunction<? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public MapReduceKeysTask<V, U> rights;
      public MapReduceKeysTask<V, U> nextRight;

      public MapReduceKeysTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceKeysTask<V, U> nextRight, ByteFunction<? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      public final U getRawResult() {
         return this.result;
      }

      public final void compute() {
         ByteFunction<? extends U> transformer;
         BiFunction<? super U, ? super U, ? extends U> reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceKeysTask<V, U>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
            }

            U r = null;

            while((p = this.advance()) != null) {
               U u;
               if ((u = transformer.apply(p.key)) != null) {
                  r = (U)(r == null ? u : reducer.apply(r, u));
               }
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceKeysTask<V, U> t = (MapReduceKeysTask)c;

               for(MapReduceKeysTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  U sr;
                  if ((sr = s.result) != null) {
                     U tr;
                     t.result = (U)((tr = t.result) == null ? sr : reducer.apply(tr, sr));
                  }
               }
            }
         }

      }
   }

   protected static final class MapReduceValuesTask<V, U> extends BulkTask<V, U> {
      public final Function<? super V, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public MapReduceValuesTask<V, U> rights;
      public MapReduceValuesTask<V, U> nextRight;

      public MapReduceValuesTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceValuesTask<V, U> nextRight, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      public final U getRawResult() {
         return this.result;
      }

      public final void compute() {
         Function<? super V, ? extends U> transformer;
         BiFunction<? super U, ? super U, ? extends U> reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceValuesTask<V, U>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
            }

            U r = null;

            while((p = this.advance()) != null) {
               U u;
               if ((u = (U)transformer.apply(p.val)) != null) {
                  r = (U)(r == null ? u : reducer.apply(r, u));
               }
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceValuesTask<V, U> t = (MapReduceValuesTask)c;

               for(MapReduceValuesTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  U sr;
                  if ((sr = s.result) != null) {
                     U tr;
                     t.result = (U)((tr = t.result) == null ? sr : reducer.apply(tr, sr));
                  }
               }
            }
         }

      }
   }

   protected static final class MapReduceEntriesTask<V, U> extends BulkTask<V, U> {
      public final Function<Entry<V>, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public MapReduceEntriesTask<V, U> rights;
      public MapReduceEntriesTask<V, U> nextRight;

      public MapReduceEntriesTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceEntriesTask<V, U> nextRight, Function<Entry<V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      public final U getRawResult() {
         return this.result;
      }

      public final void compute() {
         Function<Entry<V>, ? extends U> transformer;
         BiFunction<? super U, ? super U, ? extends U> reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceEntriesTask<V, U>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
            }

            U r = null;

            while((p = this.advance()) != null) {
               U u;
               if ((u = (U)transformer.apply(p)) != null) {
                  r = (U)(r == null ? u : reducer.apply(r, u));
               }
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceEntriesTask<V, U> t = (MapReduceEntriesTask)c;

               for(MapReduceEntriesTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  U sr;
                  if ((sr = s.result) != null) {
                     U tr;
                     t.result = (U)((tr = t.result) == null ? sr : reducer.apply(tr, sr));
                  }
               }
            }
         }

      }
   }

   protected static final class MapReduceMappingsTask<V, U> extends BulkTask<V, U> {
      public final ByteObjFunction<? super V, ? extends U> transformer;
      public final BiFunction<? super U, ? super U, ? extends U> reducer;
      public U result;
      public MapReduceMappingsTask<V, U> rights;
      public MapReduceMappingsTask<V, U> nextRight;

      public MapReduceMappingsTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceMappingsTask<V, U> nextRight, ByteObjFunction<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.reducer = reducer;
      }

      public final U getRawResult() {
         return this.result;
      }

      public final void compute() {
         ByteObjFunction<? super V, ? extends U> transformer;
         BiFunction<? super U, ? super U, ? extends U> reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceMappingsTask<V, U>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, reducer)).fork();
            }

            U r = null;

            while((p = this.advance()) != null) {
               U u;
               if ((u = transformer.apply(p.key, p.val)) != null) {
                  r = (U)(r == null ? u : reducer.apply(r, u));
               }
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceMappingsTask<V, U> t = (MapReduceMappingsTask)c;

               for(MapReduceMappingsTask<V, U> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  U sr;
                  if ((sr = s.result) != null) {
                     U tr;
                     t.result = (U)((tr = t.result) == null ? sr : reducer.apply(tr, sr));
                  }
               }
            }
         }

      }
   }

   protected static final class MapReduceKeysToDoubleTask<V> extends DoubleReturningBulkTask<V> {
      public final ByteToDoubleFunction transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public MapReduceKeysToDoubleTask<V> rights;
      public MapReduceKeysToDoubleTask<V> nextRight;

      public MapReduceKeysToDoubleTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceKeysToDoubleTask<V> nextRight, ByteToDoubleFunction transformer, double basis, DoubleBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ByteToDoubleFunction transformer;
         DoubleBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            double r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceKeysToDoubleTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceKeysToDoubleTask<V> t = (MapReduceKeysToDoubleTask)c;

               for(MapReduceKeysToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsDouble(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceValuesToDoubleTask<V> extends DoubleReturningBulkTask<V> {
      public final ToDoubleFunction<? super V> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public MapReduceValuesToDoubleTask<V> rights;
      public MapReduceValuesToDoubleTask<V> nextRight;

      public MapReduceValuesToDoubleTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceValuesToDoubleTask<V> nextRight, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToDoubleFunction<? super V> transformer;
         DoubleBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            double r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceValuesToDoubleTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.val));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceValuesToDoubleTask<V> t = (MapReduceValuesToDoubleTask)c;

               for(MapReduceValuesToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsDouble(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceEntriesToDoubleTask<V> extends DoubleReturningBulkTask<V> {
      public final ToDoubleFunction<Entry<V>> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public MapReduceEntriesToDoubleTask<V> rights;
      public MapReduceEntriesToDoubleTask<V> nextRight;

      public MapReduceEntriesToDoubleTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceEntriesToDoubleTask<V> nextRight, ToDoubleFunction<Entry<V>> transformer, double basis, DoubleBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToDoubleFunction<Entry<V>> transformer;
         DoubleBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            double r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceEntriesToDoubleTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsDouble(r, transformer.applyAsDouble(p));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceEntriesToDoubleTask<V> t = (MapReduceEntriesToDoubleTask)c;

               for(MapReduceEntriesToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsDouble(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceMappingsToDoubleTask<V> extends DoubleReturningBulkTask<V> {
      public final ToDoubleByteObjFunction<? super V> transformer;
      public final DoubleBinaryOperator reducer;
      public final double basis;
      public MapReduceMappingsToDoubleTask<V> rights;
      public MapReduceMappingsToDoubleTask<V> nextRight;

      public MapReduceMappingsToDoubleTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceMappingsToDoubleTask<V> nextRight, ToDoubleByteObjFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Double getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToDoubleByteObjFunction<? super V> transformer;
         DoubleBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            double r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceMappingsToDoubleTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceMappingsToDoubleTask<V> t = (MapReduceMappingsToDoubleTask)c;

               for(MapReduceMappingsToDoubleTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsDouble(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceKeysToLongTask<V> extends LongReturningBulkTask<V> {
      public final ByteToLongFunction transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public MapReduceKeysToLongTask<V> rights;
      public MapReduceKeysToLongTask<V> nextRight;

      public MapReduceKeysToLongTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceKeysToLongTask<V> nextRight, ByteToLongFunction transformer, long basis, LongBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ByteToLongFunction transformer;
         LongBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            long r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceKeysToLongTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsLong(r, transformer.applyAsLong(p.key));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceKeysToLongTask<V> t = (MapReduceKeysToLongTask)c;

               for(MapReduceKeysToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsLong(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceValuesToLongTask<V> extends LongReturningBulkTask<V> {
      public final ToLongFunction<? super V> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public MapReduceValuesToLongTask<V> rights;
      public MapReduceValuesToLongTask<V> nextRight;

      public MapReduceValuesToLongTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceValuesToLongTask<V> nextRight, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToLongFunction<? super V> transformer;
         LongBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            long r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceValuesToLongTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsLong(r, transformer.applyAsLong(p.val));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceValuesToLongTask<V> t = (MapReduceValuesToLongTask)c;

               for(MapReduceValuesToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsLong(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceEntriesToLongTask<V> extends LongReturningBulkTask<V> {
      public final ToLongFunction<Entry<V>> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public MapReduceEntriesToLongTask<V> rights;
      public MapReduceEntriesToLongTask<V> nextRight;

      public MapReduceEntriesToLongTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceEntriesToLongTask<V> nextRight, ToLongFunction<Entry<V>> transformer, long basis, LongBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToLongFunction<Entry<V>> transformer;
         LongBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            long r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceEntriesToLongTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsLong(r, transformer.applyAsLong(p));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceEntriesToLongTask<V> t = (MapReduceEntriesToLongTask)c;

               for(MapReduceEntriesToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsLong(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceMappingsToLongTask<V> extends LongReturningBulkTask<V> {
      public final ToLongByteObjFunction<? super V> transformer;
      public final LongBinaryOperator reducer;
      public final long basis;
      public MapReduceMappingsToLongTask<V> rights;
      public MapReduceMappingsToLongTask<V> nextRight;

      public MapReduceMappingsToLongTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceMappingsToLongTask<V> nextRight, ToLongByteObjFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Long getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToLongByteObjFunction<? super V> transformer;
         LongBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            long r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceMappingsToLongTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceMappingsToLongTask<V> t = (MapReduceMappingsToLongTask)c;

               for(MapReduceMappingsToLongTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsLong(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceKeysToIntTask<V> extends IntReturningBulkTask<V> {
      public final ByteToIntFunction transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public MapReduceKeysToIntTask<V> rights;
      public MapReduceKeysToIntTask<V> nextRight;

      public MapReduceKeysToIntTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceKeysToIntTask<V> nextRight, ByteToIntFunction transformer, int basis, IntBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ByteToIntFunction transformer;
         IntBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceKeysToIntTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsInt(r, transformer.applyAsInt(p.key));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceKeysToIntTask<V> t = (MapReduceKeysToIntTask)c;

               for(MapReduceKeysToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsInt(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceValuesToIntTask<V> extends IntReturningBulkTask<V> {
      public final ToIntFunction<? super V> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public MapReduceValuesToIntTask<V> rights;
      public MapReduceValuesToIntTask<V> nextRight;

      public MapReduceValuesToIntTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceValuesToIntTask<V> nextRight, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToIntFunction<? super V> transformer;
         IntBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceValuesToIntTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsInt(r, transformer.applyAsInt(p.val));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceValuesToIntTask<V> t = (MapReduceValuesToIntTask)c;

               for(MapReduceValuesToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsInt(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceEntriesToIntTask<V> extends IntReturningBulkTask<V> {
      public final ToIntFunction<Entry<V>> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public MapReduceEntriesToIntTask<V> rights;
      public MapReduceEntriesToIntTask<V> nextRight;

      public MapReduceEntriesToIntTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceEntriesToIntTask<V> nextRight, ToIntFunction<Entry<V>> transformer, int basis, IntBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToIntFunction<Entry<V>> transformer;
         IntBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceEntriesToIntTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsInt(r, transformer.applyAsInt(p));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceEntriesToIntTask<V> t = (MapReduceEntriesToIntTask)c;

               for(MapReduceEntriesToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsInt(t.result, s.result);
               }
            }
         }

      }
   }

   protected static final class MapReduceMappingsToIntTask<V> extends IntReturningBulkTask<V> {
      public final ToIntByteObjFunction<? super V> transformer;
      public final IntBinaryOperator reducer;
      public final int basis;
      public MapReduceMappingsToIntTask<V> rights;
      public MapReduceMappingsToIntTask<V> nextRight;

      public MapReduceMappingsToIntTask(BulkTask<V, ?> p, int b, int i, int f, Node<V>[] t, MapReduceMappingsToIntTask<V> nextRight, ToIntByteObjFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
         super(p, b, i, f, t);
         this.nextRight = nextRight;
         this.transformer = transformer;
         this.basis = basis;
         this.reducer = reducer;
      }

      public final Integer getRawResult() {
         throw new UnsupportedOperationException();
      }

      public final void compute() {
         ToIntByteObjFunction<? super V> transformer;
         IntBinaryOperator reducer;
         if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
            int r = this.basis;
            int i = this.baseIndex;

            int f;
            int h;
            while(this.batch > 0 && (h = (f = this.baseLimit) + i >>> 1) > i) {
               this.addToPendingCount(1);
               (this.rights = new MapReduceMappingsToIntTask<V>(this, this.batch >>>= 1, this.baseLimit = h, f, this.tab, this.rights, transformer, r, reducer)).fork();
            }

            while((p = this.advance()) != null) {
               r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
            }

            this.result = r;

            for(CountedCompleter<?> c = this.firstComplete(); c != null; c = c.nextComplete()) {
               MapReduceMappingsToIntTask<V> t = (MapReduceMappingsToIntTask)c;

               for(MapReduceMappingsToIntTask<V> s = t.rights; s != null; s = t.rights = s.nextRight) {
                  t.result = reducer.applyAsInt(t.result, s.result);
               }
            }
         }

      }
   }

   @FunctionalInterface
   public interface ByteBiObjByteConsumer<V, X> {
      void accept(byte var1, V var2, byte var3, X var4);
   }

   @FunctionalInterface
   public interface ByteBiObjConsumer<V, X> {
      void accept(byte var1, V var2, X var3);
   }

   @FunctionalInterface
   public interface ByteBiObjDoubleConsumer<V, X> {
      void accept(byte var1, V var2, double var3, X var5);
   }

   @FunctionalInterface
   public interface ByteBiObjFloatConsumer<V, X> {
      void accept(byte var1, V var2, float var3, X var4);
   }

   @FunctionalInterface
   public interface ByteBiObjFunction<V, X, J> {
      J apply(byte var1, V var2, X var3);
   }

   @FunctionalInterface
   public interface ByteBiObjIntConsumer<V, X> {
      void accept(byte var1, V var2, int var3, X var4);
   }

   @FunctionalInterface
   public interface ByteBiObjLongConsumer<V, X> {
      void accept(byte var1, V var2, long var3, X var5);
   }

   @FunctionalInterface
   public interface ByteBiObjShortConsumer<V, X> {
      void accept(byte var1, V var2, short var3, X var4);
   }

   @FunctionalInterface
   public interface ByteFunction<R> {
      R apply(byte var1);
   }

   @FunctionalInterface
   public interface ByteObjByteConsumer<V> {
      void accept(byte var1, V var2, byte var3);
   }

   @FunctionalInterface
   public interface ByteObjByteFunction<V, J> {
      J apply(byte var1, V var2, byte var3);
   }

   @FunctionalInterface
   public interface ByteObjConsumer<V> {
      void accept(byte var1, V var2);
   }

   @FunctionalInterface
   public interface ByteObjDoubleConsumer<V> {
      void accept(byte var1, V var2, double var3);
   }

   @FunctionalInterface
   public interface ByteObjDoubleFunction<V, J> {
      J apply(byte var1, V var2, double var3);
   }

   @FunctionalInterface
   public interface ByteObjFloatConsumer<V> {
      void accept(byte var1, V var2, float var3);
   }

   @FunctionalInterface
   public interface ByteObjFloatFunction<V, J> {
      J apply(byte var1, V var2, float var3);
   }

   @FunctionalInterface
   public interface ByteObjFunction<V, J> {
      J apply(byte var1, V var2);
   }

   @FunctionalInterface
   public interface ByteObjIntConsumer<V> {
      void accept(byte var1, V var2, int var3);
   }

   @FunctionalInterface
   public interface ByteObjIntFunction<V, J> {
      J apply(byte var1, V var2, int var3);
   }

   @FunctionalInterface
   public interface ByteObjLongConsumer<V> {
      void accept(byte var1, V var2, long var3);
   }

   @FunctionalInterface
   public interface ByteObjLongFunction<V, J> {
      J apply(byte var1, V var2, long var3);
   }

   @FunctionalInterface
   public interface ByteObjShortConsumer<V> {
      void accept(byte var1, V var2, short var3);
   }

   @FunctionalInterface
   public interface ByteObjShortFunction<V, J> {
      J apply(byte var1, V var2, short var3);
   }

   @FunctionalInterface
   public interface ByteReduceTaskOperator {
      byte reduce(byte var1, byte var2, byte var3);
   }

   @FunctionalInterface
   public interface ByteToDoubleFunction {
      double applyAsDouble(byte var1);
   }

   @FunctionalInterface
   public interface ByteToIntFunction {
      int applyAsInt(byte var1);
   }

   @FunctionalInterface
   public interface ByteToLongFunction {
      long applyAsLong(byte var1);
   }

   @FunctionalInterface
   public interface ByteTriObjConsumer<V, X, Y> {
      void accept(byte var1, V var2, X var3, Y var4);
   }

   @FunctionalInterface
   public interface ToByteFunction<T> {
      byte applyAsByte(T var1);
   }

   @FunctionalInterface
   public interface ToDoubleByteObjFunction<V> {
      double applyAsDouble(byte var1, V var2);
   }

   @FunctionalInterface
   public interface ToIntByteObjFunction<V> {
      int applyAsInt(byte var1, V var2);
   }

   @FunctionalInterface
   public interface ToLongByteObjFunction<V> {
      long applyAsLong(byte var1, V var2);
   }
}
