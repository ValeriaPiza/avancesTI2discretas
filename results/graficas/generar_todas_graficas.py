# results/graficas/generar_todas_graficas.py
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os

print("🎨 GENERANDO GRÁFICAS CON MATPLOTLIB")
print("=" * 50)

def verificar_archivos():
    """Verificar que los archivos CSV existen"""
    archivos = {
        'comparativo': 'results/graficas/analisis_comparativo.csv',
        'altura': 'results/graficas/altura_vs_tamaño.csv',
        'tiempos': 'results/graficas/tiempos_vs_tamaño.csv',
        'colisiones': 'results/graficas/analisis_colisiones.csv',
        'complejidad': 'results/graficas/analisis_complejidad.csv'
    }

    for nombre, archivo in archivos.items():
        if os.path.exists(archivo):
            print(f"✅ {nombre}: {archivo}")
        else:
            print(f"❌ {nombre}: NO ENCONTRADO - {archivo}")
            return False
    return True

def grafica_tamaño_operaciones_tiempo():
    """Gráfica principal: Tamaño vs Operaciones vs Tiempo"""
    print("\n📊 Generando gráfica: Tamaño vs Operaciones vs Tiempo")

    # Cargar datos de tus archivos CSV
    df_comp = pd.read_csv('results/graficas/analisis_comparativo.csv')
    df_altura = pd.read_csv('results/graficas/altura_vs_tamaño.csv')

    # Combinar datos
    df = pd.merge(df_comp, df_altura, on=['tamaño_dataset', 'tipo_clave'])

    # Calcular operaciones (como lo haces en Scala)
    df['operaciones_insercion'] = df['tamaño_dataset']
    df['operaciones_busqueda'] = df['tamaño_dataset'] / 2

    # Crear figura
    plt.figure(figsize=(14, 8))

    # Graficar claves numéricas
    df_num = df[df['tipo_clave'] == 'numerica']
    plt.scatter(df_num['tamaño_dataset'],
                df_num['tiempo_insercion_ms'],
                s=df_num['operaciones_insercion']/5,  # Tamaño de burbuja
                alpha=0.7, color='blue',
                label='Claves Numéricas', edgecolors='black')

    # Graficar claves textuales
    df_text = df[df['tipo_clave'] == 'textual']
    plt.scatter(df_text['tamaño_dataset'],
                df_text['tiempo_insercion_ms'],
                s=df_text['operaciones_insercion']/5,
                alpha=0.7, color='red',
                label='Claves Textuales', edgecolors='black')

    plt.xlabel('Tamaño del Árbol (número de claves)', fontsize=12, fontweight='bold')
    plt.ylabel('Tiempo de Inserción (ms)', fontsize=12, fontweight='bold')
    plt.title('Relación: Tamaño del Árbol vs Operaciones vs Tiempo de Ejecución\nB-Tree Inmutable - Dataset Netflix',
              fontsize=14, fontweight='bold')
    plt.legend()
    plt.grid(True, alpha=0.3)

    # Añadir anotaciones de tamaño
    sizes = df['tamaño_dataset'].unique()
    for size in sizes:
        plt.annotate(f'{size} ops', (size, plt.ylim()[1] * 0.9),
                     ha='center', fontsize=9, alpha=0.7)

    plt.tight_layout()
    plt.savefig('results/graficas/1_tamaño_operaciones_tiempo.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("✅ Guardada: 1_tamaño_operaciones_tiempo.png")

def grafica_tiempos_operaciones():
    """Gráfica de tiempos de inserción y búsqueda"""
    print("\n⏱️ Generando gráfica: Tiempos de Operaciones")

    df = pd.read_csv('results/graficas/tiempos_vs_tamaño.csv')

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

    # Inserción
    for tipo in df['tipo_clave'].unique():
        subset = df[df['tipo_clave'] == tipo]
        color = 'blue' if tipo == 'numerica' else 'red'
        ax1.plot(subset['tamaño_dataset'], subset['tiempo_insercion_ms'],
                 marker='o', linewidth=2, color=color, label=f'Claves {tipo.capitalize()}')

    ax1.set_xlabel('Tamaño del Dataset', fontsize=11)
    ax1.set_ylabel('Tiempo de Inserción (ms)', fontsize=11)
    ax1.set_title('Tiempo de Inserción vs Tamaño', fontsize=13, fontweight='bold')
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # Búsqueda
    for tipo in df['tipo_clave'].unique():
        subset = df[df['tipo_clave'] == tipo]
        color = 'blue' if tipo == 'numerica' else 'red'
        ax2.plot(subset['tamaño_dataset'], subset['tiempo_busqueda_ms'],
                 marker='s', linewidth=2, color=color, label=f'Claves {tipo.capitalize()}')

    ax2.set_xlabel('Tamaño del Dataset', fontsize=11)
    ax2.set_ylabel('Tiempo de Búsqueda (ms)', fontsize=11)
    ax2.set_title('Tiempo de Búsqueda vs Tamaño', fontsize=13, fontweight='bold')
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig('results/graficas/2_tiempos_operaciones.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("✅ Guardada: 2_tiempos_operaciones.png")

def grafica_altura_teorica():
    """Gráfica de altura experimental vs teórica"""
    print("\n🌳 Generando gráfica: Altura Experimental vs Teórica")

    df = pd.read_csv('results/graficas/altura_vs_tamaño.csv')

    plt.figure(figsize=(12, 7))

    for tipo in df['tipo_clave'].unique():
        subset = df[df['tipo_clave'] == tipo]
        color = 'blue' if tipo == 'numerica' else 'red'

        # Altura experimental
        plt.plot(subset['tamaño_dataset'], subset['altura_arbol'],
                 marker='o', linewidth=2, color=color,
                 label=f'Altura Experimental - {tipo.capitalize()}')

        # Altura teórica (línea punteada)
        plt.plot(subset['tamaño_dataset'], subset['altura_teorica'],
                 '--', linewidth=1.5, color=color, alpha=0.7,
                 label=f'Altura Teórica O(log n) - {tipo.capitalize()}')

    plt.xlabel('Tamaño del Dataset', fontsize=12)
    plt.ylabel('Altura del Árbol', fontsize=12)
    plt.title('Altura del B-Tree: Experimental vs Teórica', fontsize=14, fontweight='bold')
    plt.legend()
    plt.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig('results/graficas/3_altura_experimental_vs_teorica.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("✅ Guardada: 3_altura_experimental_vs_teorica.png")

def grafica_colisiones():
    """Gráfica de análisis de colisiones"""
    print("\n🔍 Generando gráfica: Análisis de Colisiones")

    df = pd.read_csv('results/graficas/analisis_colisiones.csv')

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))

    # Tasa de colisiones
    for tipo in df['tipo_clave'].unique():
        subset = df[df['tipo_clave'] == tipo]
        color = 'blue' if tipo == 'numerica' else 'red'
        ax1.bar([x + (0.2 if tipo == 'textual' else -0.2) for x in subset['tamaño_dataset']],
                subset['tasa_colisiones'] * 100,
                width=30, alpha=0.7, color=color, label=f'Claves {tipo.capitalize()}')

    ax1.set_xlabel('Tamaño del Dataset', fontsize=11)
    ax1.set_ylabel('Tasa de Colisiones (%)', fontsize=11)
    ax1.set_title('Tasa de Colisiones por Tipo de Clave', fontsize=13, fontweight='bold')
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # Eficiencia
    for tipo in df['tipo_clave'].unique():
        subset = df[df['tipo_clave'] == tipo]
        color = 'blue' if tipo == 'numerica' else 'red'
        ax2.plot(subset['tamaño_dataset'], subset['eficiencia'] * 100,
                 marker='o', linewidth=2, color=color, label=f'Claves {tipo.capitalize()}')

    ax2.set_xlabel('Tamaño del Dataset', fontsize=11)
    ax2.set_ylabel('Eficiencia de Claves Únicas (%)', fontsize=11)
    ax2.set_title('Eficiencia en Generación de Claves', fontsize=13, fontweight='bold')
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig('results/graficas/4_analisis_colisiones.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("✅ Guardada: 4_analisis_colisiones.png")

def grafica_complejidad():
    """Gráfica de análisis de complejidad computacional"""
    print("\n📈 Generando gráfica: Análisis de Complejidad")

    df = pd.read_csv('results/graficas/analisis_complejidad.csv')

    plt.figure(figsize=(12, 8))

    for tipo in df['tipo_clave'].unique():
        subset = df[df['tipo_clave'] == tipo]
        color = 'blue' if tipo == 'numerica' else 'red'

        # Tiempo experimental
        plt.plot(subset['log_n'], subset['tiempo_insercion_ms'],
                 marker='o', linewidth=2, color=color,
                 label=f'Tiempo Experimental - {tipo.capitalize()}')

        # Operaciones teóricas (normalizadas para comparación)
        max_time = subset['tiempo_insercion_ms'].max()
        max_ops = subset['operaciones_teoricas'].max()
        theoretical_normalized = subset['operaciones_teoricas'] * (max_time / max_ops)

        plt.plot(subset['log_n'], theoretical_normalized,
                 '--', linewidth=1.5, color=color, alpha=0.7,
                 label=f'Complejidad Teórica O(log n) - {tipo.capitalize()}')

    plt.xlabel('log(n) - Tamaño del Dataset (escala logarítmica)', fontsize=12)
    plt.ylabel('Tiempo de Inserción (ms) / Operaciones (normalizado)', fontsize=12)
    plt.title('Complejidad Computacional: Experimental vs Teórica', fontsize=14, fontweight='bold')
    plt.legend()
    plt.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig('results/graficas/5_complejidad_computacional.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("✅ Guardada: 5_complejidad_computacional.png")

def main():
    if not verificar_archivos():
        print("\n❌ No se pueden generar gráficas - Archivos CSV faltantes")
        print("💡 Ejecuta primero: sbt run")
        return

    # Generar todas las gráficas
    grafica_tamaño_operaciones_tiempo()
    grafica_tiempos_operaciones()
    grafica_altura_teorica()
    grafica_colisiones()
    grafica_complejidad()

    print("\n" + "=" * 50)
    print("🎉 TODAS LAS GRÁFICAS GENERADAS EXITOSAMENTE!")
    print("📁 Revisa la carpeta: results/graficas/")
    print("\n📊 Gráficas creadas:")
    print("   1. 1_tamaño_operaciones_tiempo.png")
    print("   2. 2_tiempos_operaciones.png")
    print("   3. 3_altura_experimental_vs_teorica.png")
    print("   4. 4_analisis_colisiones.png")
    print("   5. 5_complejidad_computacional.png")

if __name__ == "__main__":
    main()