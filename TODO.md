
# Todos

## Language features

[x] Words  
[x] Lists  
[x] Nested lists  
[x] Procedures  
[x] Refactor name "Function" to "Procedure"  
[x] Vararg procedures  
[] Arrays  
[] Macros  
[] Aliases  
[x] Case insensitivity  
[] Proper error handling  
[] Output  
[] Proper continuations

## Builtins

See https://people.eecs.berkeley.edu/~bh/v2ch14/manual.html for reference

### Data Structure Primitives

#### Constructors

[x] word  
[x] list  
[x] sentence  
[x] fput  
[x] lput  
[] array  
[] mdarray  
[] listtoarray  
[] arraytolist  
[] combine  
[] reverse  
[x] gensym  

#### Data Selectors

[x] first  
[] firsts  
[x] last  
[x] butfirst  
[] butfirsts  
[x] butlast  
[x] item  
[] mditem  
[] pick  
[] remove  
[] remdup  
[] quoted  

#### Data Mutators

[] setitem  
[] mdsetitem  
[] .setfirst / SETFIRST  
[] .setbf / SETBF  
[] .setitem / SETITEM  
[] push  
[] pop  
[] queue  
[] dequeue  

#### Predicates

[x] wordp  
[x] listp  
[] arrayp  
[x] emptyp  
[x] equalp  
[x] notequalp  
[] beforep  
[] .eq / EQ  
[x] memberp  
[] substringp  
[] numberp  
[] vbarredp  

#### Queries

[] count  
[] ascii  
[] rawascii  
[] char  
[] member  
[] lowercase  
[] uppercase  
[] standout  
[] parse  
[] runparse  

## Communication

### Transmitters

[x] print  
[x] type  
[x] show  

### Receivers

[x] readlist  
[x] readword  
[] readrawline  
[] readchar  
[] readchars  
[] shell  

### File Access

[] setprefix  
[] prefix  
[] openread  
[] openwrite  
[] openappend  
[] openupdate  
[] close  
[] allopen  
[] closeall  
[] erasefile  
[] dribble  
[] nodribble  
[] setread  
[] setwrite  
[] reader  
[] writer  
[] setreadpos  
[] setwritepos  
[] readpos  
[] writepos  
[] eofp  
[] filep  

### Terminal Access

[] keyp  
[] cleartext  
[] setcursor  
[] cursor  
[] setmargins  
[] settextcolor  
[] increasefont  
[] settextsize  
[] textsize  
[] setfont  
[] font  