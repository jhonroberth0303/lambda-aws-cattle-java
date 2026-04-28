# Estandares Frontend - cattle-front

## Objetivo

Definir convenciones practicas para escribir y revisar codigo frontend en `cattle-front` a partir de la SPA real del repositorio.

Este documento describe como trabajar con React, Vite, JSX, hooks y servicios HTTP ligeros sin inventar una arquitectura que hoy no existe en el codigo.

## Evidencia revisada

- `package.json`
- `eslint.config.js`
- `src/App.jsx`
- `src/components/Bovines/list/BovineList.jsx`
- `src/services/bovinesServices.ts`
- `src/components/Bovines/hooks/useBovineForm.ts`

## Stack vigente

- React 19
- React Router DOM 7
- Vite 7
- JSX como formato dominante de componentes
- mezcla de JavaScript y TypeScript en componentes, hooks y utilidades
- Axios y `fetch` nativo para integracion HTTP
- ESLint plano (`eslint.config.js`)
- `vite-plugin-pwa` presente en dependencias

## Principios de codigo

1. Mantener componentes orientados a pantalla, lista, tarjeta o formulario con responsabilidad clara.
2. Separar presentacion, hooks y servicios cuando el modulo ya tenga esa division.
3. Evitar introducir una capa de abstraccion nueva si el modulo vigente es mas simple.
4. Conservar consistencia de nombres y rutas con la SPA actual.
5. Hacer explicitos los acoplamientos de endpoints y configuracion cuando todavia esten hardcodeados.

## Organizacion recomendada

El patron actual mas estable es por dominio dentro de `src/components`:

- `Bovines/`
- `MilkDashboard/`
- `Paddock/`
- `AgroChat/`
- `Shared/`

Dentro de cada dominio, preferir separar:

- `list/` para vistas de coleccion
- `cards/` para items visuales
- `forms/` para captura y edicion
- `hooks/` para logica reutilizable local del dominio

## Convenciones de nombres

### Componentes y archivos

- `PascalCase` para componentes React y archivos de componentes
- `camelCase` para funciones y variables
- prefijo `use` para hooks

Ejemplos reales:

- `BovineList.jsx`
- `BovineCard.jsx`
- `AgroChatSimplePage.jsx`
- `useBovineForm.ts`

### Servicios

- nombre de archivo en `camelCase` con sufijo `Service` o `Services` si el archivo ya sigue ese patron
- exponer funciones pequenas por endpoint o contrato

Ejemplos reales:

- `bovinesServices.ts`
- `milkingService.js`

### Booleanos y handlers

- usar prefijos `is`, `has`, `should` para booleanos
- usar `handle...` para handlers de UI

Ejemplos reales:

- `isAuthenticated`
- `handleChange`
- `handleScanTag`

## Patrones de implementacion

### Componentes funcionales

Usar componentes funcionales con hooks como patron por defecto.

Preferido:

```jsx
function BovineList() {
  const [bovines, setBovines] = useState([]);

  useEffect(() => {
    fetch(getBovinesSummaryEndpoint())
      .then((res) => res.json())
      .then((data) => setBovines(data))
      .catch((err) => console.error(err));
  }, []);

  return <div>{/* UI */}</div>;
}
```

### Hooks de dominio

Cuando un formulario o pantalla concentra demasiada logica, extraerla a un hook local del dominio.

Ejemplo confirmado:

- `useBovineForm.ts`

Responsabilidades adecuadas del hook:

- estado del formulario
- carga de detalle puntual
- submit y update
- helpers de UI o calculos ligeros ligados al formulario

### Servicios HTTP

Mantener funciones chicas y explicitas.

Recomendado:

- un helper para endpoint de listado si ese contrato es distinto
- otro helper para endpoint CRUD si la escritura usa otra ruta
- funciones `create`, `update`, `getById` con nombres directos

La correccion reciente de bovinos deja el patron esperado:

- `getBovinesSummaryEndpoint()` para `/summary`
- `getBovinesCrudEndpoint()` para `/bovines`

### Routing

- declarar rutas en `App.jsx`
- usar `RequireAuth` o wrapper equivalente para zona privada
- mantener paths alineados con la navegacion real del producto

## Estado y datos

- preferir `useState` y `useEffect` para estado local de pantalla cuando alcance
- no introducir estado global si el slice sigue siendo local y simple
- derivar datos presentacionales en el componente o hook solo cuando no exista ya ese calculo en backend
- si backend ya entrega estados calculados, no duplicar la misma regla de negocio en varios componentes

## Estilos y CSS

- mantener CSS por componente o por pantalla cuando ya exista ese patron
- usar nombres de clase suficientemente especificos para evitar colisiones
- evitar refactors visuales globales dentro de cambios funcionales pequenos

Ejemplos consistentes:

- `BovineList.css`
- `DashboardLayout.css`
- `AddBovine.css`

## Linting y validacion

### Configuracion vigente

`eslint.config.js` confirma:

- ESLint 9 con flat config
- `@eslint/js`
- `eslint-plugin-react-hooks`
- `eslint-plugin-react-refresh`
- regla activa de `no-unused-vars`

### Comandos vigentes

- `npm run dev`
- `npm run build`
- `npm run lint`

### Gaps de enforcement actuales

No hay evidencia directa en la configuracion revisada de:

- Prettier activo
- testing frontend operativo como parte del flujo principal
- lint dedicado para archivos `ts` o `tsx`

Conclusiones practicas:

- la consistencia visual depende hoy mas del estilo existente y de revision humana que de formateo automatico documentado
- hay archivos TypeScript en el repo, pero el lint observado esta enfocado en `js` y `jsx`

## Reglas practicas de revision

1. El componente tiene una responsabilidad entendible desde su nombre.
2. La logica repetida de formulario o carga se mueve a hook del dominio.
3. Los endpoints de lectura y escritura no se mezclan por conveniencia.
4. Los nombres de clases CSS y componentes siguen el estilo local del modulo.
5. El cambio pasa `npm run lint` cuando afecte archivos cubiertos por ESLint.
6. Si se introduce TypeScript nuevo, no degradar tipos existentes a `any` salvo borde justificado.
7. Si backend ya calcula un estado, el frontend solo lo presenta o deriva un fallback ligero.