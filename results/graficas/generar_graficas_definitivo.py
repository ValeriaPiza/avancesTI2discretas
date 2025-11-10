# results/graficas/generar_graficas_definitivo.py
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os
import sys

print("GENERADOR DE GRÁFICAS DEFINITIVO")
print("=" * 50)

def cargar_y_unificar_datos():
    """Cargar y unificar datos de todos los CSV"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    datos = {}

    print("CARGANDO ARCHIVOS CSV...")

    try:
        # Cargar archivos individuales
        datos['altura'] = pd.read_csv(os.path.join(script_dir, 'altura_vs_tamaño.csv'))
        datos['colisiones'] = pd.read_csv(os.path.join(script_dir, 'analisis_colisiones.csv'))
        datos['comparativo'] = pd.read_csv(os.path.join(script_dir, 'analisis_comparativo.csv'))
        datos['complejidad'] = pd.read_csv(os.path.join(script_dir, 'analisis_complejidad.csv'))
        datos['tiempos'] = pd.read_csv(os.path.join(script_dir, 'tiempos_vs_tamaño.csv'))

        # UNIFICAR: Corregir inconsistencias en 'tipo_clave'
        for key in datos:
            if 'tipo_clave' in datos[key].columns:
                # Unificar "Numérica" -> "numerica" y "Textual" -> "textual"
                datos[key]['tipo_clave'] = datos[key]['tipo_clave'].str.lower()

        print("Todos los archivos cargados y unificados")

        # Crear dataset unificado para gráficas principales
        df_unificado = datos['comparativo'].copy()
        df_unificado['tipo_clave'] = df_unificado['tipo_clave'].str.lower()

        # Agregar datos de altura teórica
        df_altura = datos['altura'].copy()
        df_unificado = pd.merge(df_unificado, df_altura[['tamaño_dataset', 'tipo_clave', 'altura_teorica']],
                                on=['tamaño_dataset', 'tipo_clave'], how='left')

        datos['unificado'] = df_unificado
        return datos

    except Exception as e:
        print(f"Error cargando datos: {e}")
        return None

def grafica_principal_tamaño_operaciones_tiempo(datos):
    """GRÁFICA PRINCIPAL: Tamaño vs Operaciones vs Tiempo - VERSIÓN CORREGIDA"""
    print("\nGENERANDO GRÁFICA PRINCIPAL...")

    try:
        df = datos['unificado']

        # VALIDACIÓN CRÍTICA: Filtrar datos inválidos
        df = df[df['tiempo_insercion_ms'] > 0]  # Eliminar tiempos negativos o cero
        df = df[df['tamaño_dataset'] > 0]       # Eliminar tamaños inválidos

        print(f"Datos válidos para gráfica: {len(df)} registros")
        print(f"Rango de tiempos: {df['tiempo_insercion_ms'].min():.4f} - {df['tiempo_insercion_ms'].max():.4f} ms")
        print(f"Rango de tamaños: {df['tamaño_dataset'].min()} - {df['tamaño_dataset'].max()} claves")

        # Calcular operaciones (para tamaño de puntos)
        df['operaciones_insercion'] = df['tamaño_dataset']

        # Crear gráfica
        plt.figure(figsize=(14, 8))

        # Colores consistentes
        colores = {'numerica': '#1f77b4', 'textual': '#ff7f0e'}
        marcadores = {'numerica': 'o', 'textual': 's'}

        for tipo_clave in ['numerica', 'textual']:
            df_tipo = df[df['tipo_clave'] == tipo_clave]
            if len(df_tipo) > 0:
                # TAMAÑO DE PUNTOS CORREGIDO - mucho más pequeño
                tamanos_puntos = np.sqrt(df_tipo['operaciones_insercion']) * 2  # Escala logarítmica

                plt.scatter(
                    df_tipo['tamaño_dataset'],
                    df_tipo['tiempo_insercion_ms'],
                    s=tamanos_puntos,  # Tamaño proporcional corregido
                    alpha=0.7,
                    color=colores[tipo_clave],
                    marker=marcadores[tipo_clave],
                    label=f'Claves {tipo_clave.capitalize()}',
                    edgecolors='black',
                    linewidth=0.5
                )

        plt.xlabel('Tamaño del Árbol (número de claves)', fontsize=12, fontweight='bold')
        plt.ylabel('Tiempo de Inserción (ms)', fontsize=12, fontweight='bold')
        plt.title('Relación: Tamaño del Árbol vs Operaciones vs Tiempo de Ejecución\nB-Tree Inmutable - Dataset Netflix',
                  fontsize=14, fontweight='bold')
        plt.legend()
        plt.grid(True, alpha=0.3)

        # Añadir anotaciones de tamaño (solo para algunos puntos clave)
        tamanos_unicos = sorted(df['tamaño_dataset'].unique())
        for tamaño in tamanos_unicos[::max(1, len(tamanos_unicos)//5)]:  # Mostrar ~5 anotaciones
            plt.annotate(f'{tamaño} claves',
                         (tamaño, plt.ylim()[1] * 0.9),
                         ha='center', fontsize=9, alpha=0.7)

        # Asegurar que los ejes empiecen en 0 o valores positivos
        plt.xlim(left=0)
        plt.ylim(bottom=0)

        plt.tight_layout()
        plt.savefig('1_relacion_tamaño_operaciones_tiempo.png', dpi=300, bbox_inches='tight')
        plt.close()

        print("Gráfica principal guardada: 1_relacion_tamaño_operaciones_tiempo.png")
        return True

    except Exception as e:
        print(f"Error en gráfica principal: {e}")
        import traceback
        traceback.print_exc()
        return False

def grafica_comparativa_tiempos(datos):
    """Gráfica comparativa de tiempos de inserción y búsqueda"""
    print("\nGENERANDO GRÁFICA COMPARATIVA DE TIEMPOS...")

    try:
        df = datos['tiempos']

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

        colores = {'numerica': '#1f77b4', 'textual': '#ff7f0e'}

        # Inserción
        for tipo_clave in ['numerica', 'textual']:
            df_tipo = df[df['tipo_clave'] == tipo_clave]
            if len(df_tipo) > 0:
                ax1.plot(df_tipo['tamaño_dataset'], df_tipo['tiempo_insercion_ms'],
                         marker='o', linewidth=2, color=colores[tipo_clave],
                         label=f'Claves {tipo_clave.capitalize()}')

        ax1.set_xlabel('Tamaño del Dataset', fontsize=11)
        ax1.set_ylabel('Tiempo de Inserción (ms)', fontsize=11)
        ax1.set_title('Tiempo de Inserción vs Tamaño', fontsize=13, fontweight='bold')
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        # Búsqueda
        for tipo_clave in ['numerica', 'textual']:
            df_tipo = df[df['tipo_clave'] == tipo_clave]
            if len(df_tipo) > 0:
                ax2.plot(df_tipo['tamaño_dataset'], df_tipo['tiempo_busqueda_ms'],
                         marker='s', linewidth=2, color=colores[tipo_clave],
                         label=f'Claves {tipo_clave.capitalize()}')

        ax2.set_xlabel('Tamaño del Dataset', fontsize=11)
        ax2.set_ylabel('Tiempo de Búsqueda (ms)', fontsize=11)
        ax2.set_title('Tiempo de Búsqueda vs Tamaño', fontsize=13, fontweight='bold')
        ax2.legend()
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig('2_comparativa_tiempos.png', dpi=300, bbox_inches='tight')
        plt.close()

        print("Gráfica comparativa guardada: 2_comparativa_tiempos.png")
        return True

    except Exception as e:
        print(f"Error en gráfica comparativa: {e}")
        return False

def grafica_altura_teorica_vs_experimental(datos):
    """Gráfica de altura teórica vs experimental"""
    print("\nGENERANDO GRÁFICA ALTURA TEÓRICA VS EXPERIMENTAL...")

    try:
        df = datos['altura']

        plt.figure(figsize=(12, 7))

        colores = {'numerica': '#1f77b4', 'textual': '#ff7f0e'}

        for tipo_clave in ['numerica', 'textual']:
            df_tipo = df[df['tipo_clave'] == tipo_clave]
            if len(df_tipo) > 0:
                # Experimental
                plt.plot(df_tipo['tamaño_dataset'], df_tipo['altura_arbol'],
                         marker='o', linewidth=2, color=colores[tipo_clave],
                         label=f'Experimental - {tipo_clave.capitalize()}')

                # Teórica
                plt.plot(df_tipo['tamaño_dataset'], df_tipo['altura_teorica'],
                         '--', linewidth=1.5, color=colores[tipo_clave], alpha=0.7,
                         label=f'Teórica - {tipo_clave.capitalize()}')

        plt.xlabel('Tamaño del Dataset', fontsize=12)
        plt.ylabel('Altura del Árbol', fontsize=12)
        plt.title('Altura del B-Tree: Experimental vs Teórica', fontsize=14, fontweight='bold')
        plt.legend()
        plt.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig('3_altura_teorica_vs_experimental.png', dpi=300, bbox_inches='tight')
        plt.close()

        print("Gráfica altura guardada: 3_altura_teorica_vs_experimental.png")
        return True

    except Exception as e:
        print(f"Error en gráfica de altura: {e}")
        return False

def grafica_analisis_colisiones(datos):
    """Gráfica de análisis de colisiones"""
    print("\nGENERANDO GRÁFICA ANÁLISIS DE COLISIONES...")

    try:
        df = datos['colisiones']

        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

        colores = {'numerica': '#1f77b4', 'textual': '#ff7f0e'}

        # Tasa de colisiones
        for tipo_clave in ['numerica', 'textual']:
            df_tipo = df[df['tipo_clave'] == tipo_clave]
            if len(df_tipo) > 0:
                ax1.bar([x + (0.2 if tipo_clave == 'textual' else -0.2) for x in df_tipo['tamaño_dataset']],
                        df_tipo['tasa_colisiones'] * 100,
                        width=30, alpha=0.7, color=colores[tipo_clave],
                        label=f'Claves {tipo_clave.capitalize()}')

        ax1.set_xlabel('Tamaño del Dataset', fontsize=11)
        ax1.set_ylabel('Tasa de Colisiones (%)', fontsize=11)
        ax1.set_title('Tasa de Colisiones por Tipo de Clave', fontsize=13, fontweight='bold')
        ax1.legend()
        ax1.grid(True, alpha=0.3)

        # Eficiencia
        for tipo_clave in ['numerica', 'textual']:
            df_tipo = df[df['tipo_clave'] == tipo_clave]
            if len(df_tipo) > 0:
                ax2.plot(df_tipo['tamaño_dataset'], df_tipo['eficiencia'] * 100,
                         marker='o', linewidth=2, color=colores[tipo_clave],
                         label=f'Claves {tipo_clave.capitalize()}')

        ax2.set_xlabel('Tamaño del Dataset', fontsize=11)
        ax2.set_ylabel('Eficiencia de Claves Únicas (%)', fontsize=11)
        ax2.set_title('Eficiencia en Generación de Claves', fontsize=13, fontweight='bold')
        ax2.legend()
        ax2.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig('4_analisis_colisiones.png', dpi=300, bbox_inches='tight')
        plt.close()

        print("Gráfica colisiones guardada: 4_analisis_colisiones.png")
        return True

    except Exception as e:
        print(f"Error en gráfica de colisiones: {e}")
        return False

def main():
    # Cargar datos
    datos = cargar_y_unificar_datos()

    if datos is None:
        print("No se pudieron cargar los datos")
        sys.exit(1)

    # Generar gráficas
    exitos = 0
    exitos += grafica_principal_tamaño_operaciones_tiempo(datos)
    exitos += grafica_comparativa_tiempos(datos)
    exitos += grafica_altura_teorica_vs_experimental(datos)
    exitos += grafica_analisis_colisiones(datos)

    print(f"\n{'='*50}")
    print(f"GENERACIÓN COMPLETADA: {exitos}/4 gráficas creadas")
    print("Revisa la carpeta results/graficas/")

if __name__ == "__main__":
    main()