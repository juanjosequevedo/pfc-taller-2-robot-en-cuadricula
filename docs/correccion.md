En este informe explicamos por qué cada función cumple lo que pide
el taller y por qué sus llamadas llegan a un resultado.

Revisamos los casos básicos y después las órdenes que contienen otras.

Usamos inducción: suponemos que una parte más pequeña funciona
y explicamos por qué la orden completa también lo hace.

En la auxiliar de cola usamos un invariante, que es una condición
que se mantiene durante el recorrido.

Llamamos $E(e,o)$ al estado esperado al ejecutar la orden $o$
desde el estado $e$.

Las fórmulas de coordenadas y conteos se consideran para resultados
que caben en `Int`, el tipo que utiliza el modelo entregado.

## 1. Corrección de `girar`

### Qué debe calcular

Debe devolver la dirección que queda después de un giro de
noventa grados.

| Dirección actual | Derecha | Izquierda |
|---|---|---|
| Norte | Este | Oeste |
| Este | Sur | Norte |
| Sur | Oeste | Este |
| Oeste | Norte | Sur |

### Justificación

El `match` contiene las ocho parejas de la tabla. Todas las
combinaciones permitidas tienen una respuesta.

Por ejemplo:

$$
girar(Este,Derecha)=Sur
$$

$$
girar(Oeste,Izquierda)=Sur
$$

Los resultados coinciden con el giro solicitado.

### Terminación

La función escoge un caso y devuelve su dirección. No necesita
llamarse de nuevo.

## 2. Corrección de `ejecutar`

### Casos básicos

Sea el estado $e=(x,y,d)$.

Los avances deben producir:

$$
E((x,y,Norte),Avanzar(n))=(x,y+n,Norte)
$$

$$
E((x,y,Este),Avanzar(n))=(x+n,y,Este)
$$

$$
E((x,y,Sur),Avanzar(n))=(x,y-n,Sur)
$$

$$
E((x,y,Oeste),Avanzar(n))=(x-n,y,Oeste)
$$

El código realiza esas operaciones y mantiene la dirección.
Con un número negativo se obtiene el movimiento contrario.

Para un giro:

$$
E((x,y,d),Girar(s))=(x,y,girar(d,s))
$$

Las coordenadas se conservan. Por la corrección de `girar`,
la nueva dirección es la indicada.

### Repeticiones

Si $v\leq 0$, la orden no se ejecuta:

$$
E(e,Repetir(v,o))=e
$$

El programa devuelve ese mismo estado.

Para $v>0$, suponemos que ejecutar el contenido es correcto
y que repetirlo $v-1$ veces también produce la respuesta esperada.

La expresión del código es:

```scala
ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))
```

Primero ejecuta una vez y después realiza las restantes:

$$
E(e,Repetir(v,o)) = E(E(e,o),Repetir(v-1,o))
$$

Así se completan una ejecución más las $v-1$ pendientes,
es decir, las $v$ solicitadas.

### Secuencias: estado y cambios

Suponemos que las órdenes contenidas se ejecutan correctamente
desde cualquier estado.

Sean $e_0$ el estado de entrada y $L$ la lista completa.

- **Estado de la auxiliar:** $(estado,pendientes)$.
- **Inicio:** $(e_0,L)$.
- **Final:** `pendientes` es `Nil`.
- **Invariante:**

$$
E(estado,Secuencia(pendientes)) = E(e_0,Secuencia(L))
$$

La igualdad dice que ejecutar lo que falta desde el estado actual
debe dar el mismo resultado que ejecutar todo desde el comienzo.

Si los pendientes son $c::r$, el cambio es:

$$
(estado,c::r)\rightarrow(E(estado,c),r)
$$

### Demostración de la auxiliar

**1. Al comenzar.**

El estado vale $e_0$ y los pendientes son $L$:

$$
E(e_0,Secuencia(L))=E(e_0,Secuencia(L))
$$

Se cumple el invariante.

**2. Al continuar.**

Primero se ejecuta la cabeza y luego se procesa la cola.
Por la definición de una secuencia:

$$
E(estado,Secuencia(c::r)) = E(E(estado,c),Secuencia(r))
$$

Por tanto, el cambio conserva el resultado completo.

**3. Al terminar.**

La secuencia vacía no modifica el estado:

$$
E(estado,Secuencia(Nil))=estado
$$

Por el invariante, ese valor es la respuesta de ejecutar toda la lista.

**4. Por qué llega al final.**

La auxiliar retira un elemento por llamada. Las repeticiones
positivas disminuyen en uno y las órdenes interiores son finitas.

Los avances, giros y casos vacíos terminan directamente.
Por eso se completa la ejecución.

## 3. Corrección de los conteos

### 3.1. `cuantasBasicas`

Llamamos $B(o)$ al número de avances y giros que se ejecutarían.

#### Casos base

$$
B(Avanzar(n))=1
$$

$$
B(Girar(s))=1
$$

$$
B(Secuencia(Nil))=0
$$

El programa devuelve esos valores. Un avance de cero cuenta como
una orden básica, aunque no cambie la posición.

#### Caso de repetición

$$
B(Repetir(v,o))=\begin{cases}0 & \text{si }v\leq 0\\v\cdot B(o) & \text{si }v>0\end{cases}
$$

Suponiendo correcto el conteo del contenido, multiplicarlo por
las veces produce el total de acciones.

#### Caso de secuencia

Para una cabeza $c$ y una cola $r$:

$$
B(Secuencia(c::r))=B(c)+B(Secuencia(r))
$$

La hipótesis es que ambos conteos son correctos.
Su suma incluye todas las acciones de la lista, respetando
las repeticiones que contenga cada elemento.

#### Terminación

Cada llamada entra en una orden interior o continúa con una
lista más corta. Se llega a una básica o a un caso vacío.

### 3.2. `profundidad`

Llamamos $P(o)$ al número de niveles de una orden.

#### Casos base

$$
P(Avanzar(n))=P(Girar(s))=1
$$

$$
P(Secuencia(Nil))=1
$$

Estos resultados coinciden con el enunciado.

#### Caso de repetición

$$
P(Repetir(v,o))=1+P(o)
$$

Si el contenido tiene la profundidad correcta, sumar uno incluye
la repetición que lo contiene.

Las veces no afectan este cálculo. Se mide la estructura,
aunque la orden no llegue a ejecutarse.

#### Caso de secuencia

El código compara:

$$
1+P(c)
$$

y:

$$
P(Secuencia(r))
$$

El resultado esperado es:

$$
P(Secuencia(c::r)) = \max(1+P(c),P(Secuencia(r)))
$$

La cabeza necesita sumar el nivel exterior. La llamada sobre
la cola ya cuenta el de la secuencia.

Suponiendo correctos los valores interiores, el `if` selecciona
el mayor y obtiene la rama más profunda.

Cuando la cola está vacía, su profundidad vale uno. Esto no altera
la respuesta, porque la cabeza con su nivel exterior vale al menos dos.

#### Terminación

Se recorren las órdenes interiores y se reduce la lista.
Los casos básicos devuelven un número directamente.

## 4. Corrección de las rutinas

### 4.1. `componer`

Construye:

```scala
Secuencia(List(o1, o2))
```

Por la corrección de las secuencias:

$$
E(e,componer(o_1,o_2)) = E(E(e,o_1),o_2)
$$

Así queda justificado que ejecutar la compuesta equivale a
ejecutar primero una parte y después la otra.

La función termina al construir la orden.

### 4.2. `rutina`

Su resultado es:

$$
rutina(p)(s)(v) = Repetir(v,Secuencia(List(Avanzar(p),Girar(s))))
$$

`componer` coloca el avance antes del giro. `Repetir` establece
cuántas veces se ejecuta ese par.

Recibir los argumentos en grupos permite fijar los pasos y el
sentido antes de indicar las veces. El resultado conserva
el orden solicitado.

La construcción termina directamente. Su ejecución queda
justificada por lo demostrado para `ejecutar`.

### 4.3. `aplicarA`

#### Caso base

$$
aplicarA(Nil,o)=Nil
$$

Sin estados de entrada, no hay resultados que calcular.

#### Caso de inducción

Suponemos que `aplicarA(r, o)` procesa correctamente el resto.

Para una cabeza $e$:

$$
aplicarA(e::r,o) = E(e,o)::aplicarA(r,o)
$$

La primera ejecución es correcta por el punto 2.
La llamada sobre la cola también lo es por la hipótesis.

Al unirlas con `::`, cada respuesta ocupa el lugar correspondiente
y cada recorrido parte de su propio estado.

#### Terminación

La lista pierde un elemento en cada llamada. Además,
cada ejecución de la orden termina.

### 4.4. `transformar`

Llamamos $T(o,f)$ a la transformación esperada.

#### Casos base

$$
T(Avanzar(n),f)=f(Avanzar(n))
$$

$$
T(Girar(s),f)=f(Girar(s))
$$

$$
T(Secuencia(Nil),f)=Secuencia(Nil)
$$

El código devuelve esos resultados.

#### Repeticiones

Suponemos que el contenido se transforma correctamente:

$$
T(Repetir(v,o),f)=Repetir(v,T(o,f))
$$

Se conservan las veces y se cambia solo la orden interior.

#### Secuencias

Para una lista de órdenes:

$$
T(Secuencia(List(o_1,\ldots,o_k)),f) = Secuencia(List(T(o_1,f),\ldots,T(o_k,f)))
$$

`map` transforma cada elemento y conserva su lugar.
Por la hipótesis, los resultados interiores cumplen lo pedido.

`f` se aplica a las básicas originales. Si devuelve otra estructura,
se utiliza tal como la entrega.

#### Terminación

La función entra en un número finito de partes. Termina siempre
que cada aplicación de `f` también lo haga.

## 5. Corrección de `simplificar`

### Qué debe conservar

Llamamos $S(o)$ a la versión simplificada.

Queremos demostrar:

$$
E(e,o)=E(e,S(o))
$$

Se conserva la posición y la dirección finales.
Los pasos intermedios pueden ser diferentes.

### Regla 1: avanzar cero

Sumar o restar cero deja las coordenadas iguales:

$$
x+0=x
$$

$$
y+0=y
$$

La dirección tampoco cambia. Por eso:

$$
E(e,Avanzar(0))=e=E(e,Secuencia(Nil))
$$

El reemplazo mantiene el resultado.

### Regla 2: repetición no positiva

`ejecutar` devuelve el estado recibido cuando las veces son
cero o negativas:

$$
v\leq 0 \Rightarrow E(e,Repetir(v,o))=e
$$

Una secuencia vacía hace lo mismo, así que puede sustituirla.

### Regla 3: repetir una sola vez

Ejecutar una vez equivale a ejecutar directamente:

$$
E(e,Repetir(1,o))=E(e,o)
$$

Suponemos que la simplificación interior conserva su efecto:

$$
E(e,o)=E(e,S(o))
$$

Entonces se puede devolver `simplificar(orden)`.

### Regla 4: sumar avances seguidos

Los dos avances usan la misma dirección porque no hay un giro
entre ellos.

Para el norte:

$$
(y+a)+b=y+(a+b)
$$

Para el sur:

$$
(y-a)-b=y-(a+b)
$$

Las mismas igualdades se cumplen sobre $x$ al mirar al este
o al oeste.

Por tanto:

$$
E(e,Secuencia(List(Avanzar(a),Avanzar(b)))) = E(e,Avanzar(a+b))
$$

Si la suma da cero, también se puede eliminar ese avance
por la primera regla.

### Regla 5: secuencia con una orden

Por la definición de ejecución:

$$
E(e,Secuencia(List(o))) = E(E(e,o),Secuencia(Nil)) = E(e,o)
$$

Quitar la secuencia exterior conserva el efecto de su único elemento.

### Regla 6: retirar vacíos

Una orden vacía entrega el mismo estado que recibe.

Al quitarla de una lista, la siguiente orden comienza desde
la misma posición y dirección. El resultado final se mantiene.

Si el contenido simplificado de una repetición queda vacío,
repetirlo tampoco cambia el estado:

$$
E(e,Repetir(v,Secuencia(Nil)))=e
$$

Por eso también se elimina esa repetición.

### Secuencias interiores

El código coloca las órdenes de una secuencia interior en
la lista que la contiene.

Por ejemplo, una secuencia con $a$ y otra secuencia con $b$ y $c$
produce:

$$
E(E(E(e,a),b),c)
$$

Una lista con $a$, $b$ y $c$ da la misma expresión.
Se conserva el orden de ejecución, así que se puede quitar
esa separación interior.

### Corrección de las auxiliares

Llamamos $L(l)$ al resultado de `simplificarLista(l)` y
$C(l)$ al de `combinarAvances(l)`.

Para la primera auxiliar queremos:

$$
E(e,Secuencia(l))=E(e,Secuencia(L(l)))
$$

Con `Nil`, ambos lados dejan el estado igual.

Para una lista no vacía, suponemos que simplificar la cabeza
y procesar el resto conservan sus efectos desde cualquier estado.

La auxiliar reduce la cabeza y luego puede quitar un vacío,
incorporar una secuencia interior o conservar la orden obtenida.

Cada posibilidad corresponde a una regla ya justificada.
Al continuar con el resto en su orden original, se mantiene
la igualdad completa.

Para la segunda buscamos:

$$
E(e,Secuencia(l))=E(e,Secuencia(C(l)))
$$

La lista vacía cumple la igualdad.

Si aparece un avance de cero, se retira por la regla 1.
Si hay dos avances contiguos, se suman por la regla 4.
En otro caso se conserva la cabeza y se procesa el resto.

Cada llamada trabaja con una lista más corta. Suponiendo correcto
ese resultado, la lista completa conserva su efecto.

### Órdenes compuestas

La hipótesis es que simplificar las partes interiores mantiene
su resultado desde cualquier estado.

En una repetición, cada ejecución del contenido simplificado
produce el mismo estado que antes. Por eso la cantidad de veces
restantes también conserva el resultado.

En una secuencia, cada parte entrega a la siguiente el mismo
estado que la versión original.

Además:

$$
E(e,Secuencia(l)) = E(e,Secuencia(L(l))) = E(e,Secuencia(C(L(l))))
$$

La revisión final solo devuelve una secuencia vacía, su único
elemento o la lista reducida. Esos casos ya están justificados.

Así se obtiene:

$$
E(e,o)=E(e,S(o))
$$

### Por qué se aplican las reducciones necesarias

Primero se simplifica el contenido y se quitan los vacíos.
Las secuencias interiores se incorporan a la lista exterior.

Después se unen los avances que quedaron juntos.
Al obtener una suma, `combinarAvances` vuelve a revisarla antes
de avanzar. Esto permite continuar sumando o quitar un cero nuevo.

Por eso, dentro de las reglas revisadas, no hace falta reiniciar
toda la orden mediante una pasada general adicional.

### Terminación

Las llamadas sobre órdenes interiores trabajan con estructuras
más pequeñas. La simplificación elimina partes o conserva las que
ya estaban; no aumenta su tamaño.

`simplificarLista` recorre listas finitas. Si vuelve a entrar en una
secuencia obtenida, procesa su contenido, que tiene menos partes
que la secuencia que lo contiene.

`combinarAvances` reduce la cantidad de elementos en cada llamada:
elimina uno, junta dos en uno o continúa con la cola.

Por eso se llega a los casos básicos o vacíos y la función termina.