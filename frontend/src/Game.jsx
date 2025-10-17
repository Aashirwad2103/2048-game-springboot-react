import React, { useEffect, useRef, useState } from 'react'

const API = '/api/game'

function fetchJson(url, opts) {
  return fetch(url, { ...opts, headers: { 'Content-Type': 'application/json' } })
    .then(async res => {
      if (!res.ok) {
        const text = await res.text()
        throw new Error(`HTTP ${res.status} - ${text}`)
      }
      return res.json()
    })
}

export default function Game(){
  const [game, setGame] = useState(null)
  const [loading, setLoading] = useState(false)
  const gameIdRef = useRef(null)

  useEffect(() => { createNew(4) }, [])

  // handle keyboard
  useEffect(() => {
    const handleKey = e => {
      const map = {
        ArrowUp: 'UP', ArrowDown: 'DOWN', ArrowLeft: 'LEFT', ArrowRight: 'RIGHT',
        w: 'UP', s: 'DOWN', a: 'LEFT', d: 'RIGHT'
      }
      const dir = map[e.key]
      if (dir) move(dir)
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  })

  function createNew(size = 4){
    setLoading(true)
    fetchJson(API, { method: 'POST', body: JSON.stringify({ size }) })
      .then(g => { setGame(g); gameIdRef.current = g.id })
      .catch(err => alert('Error creating game: ' + err.message))
      .finally(() => setLoading(false))
  }

  function move(direction){
    if (!gameIdRef.current || game.over || game.won) return
    setLoading(true)
    fetchJson(`${API}/${gameIdRef.current}/move`, {
      method: 'POST',
      body: JSON.stringify({ direction })
    })
      .then(g => setGame(g))
      .catch(err => alert('Move error: ' + err.message))
      .finally(() => setLoading(false))
  }

  function restart(size){
    if (!gameIdRef.current) { createNew(size); return }
    setLoading(true)
    fetchJson(`${API}/${gameIdRef.current}/restart`, {
      method: 'POST',
      body: JSON.stringify({ size })
    })
      .then(g => setGame(g))
      .catch(err => alert('Restart error: ' + err.message))
      .finally(() => setLoading(false))
  }

  if (!game) return <div>Loading ...</div>

  return (
    <div className="game-root">
      <div className="header">
        <div style={{display:'flex',gap:12,alignItems:'center'}}>
          <h2 style={{margin:0}}>2048</h2>
          <div className="score">Score: <strong>{game.score}</strong></div>
        </div>

        <div style={{display:'flex',gap:8,alignItems:'center'}}>
          <label>
            Size:
            <select defaultValue={game.size} onChange={e => createNew(Number(e.target.value))}>
              {[3,4,5,6].map(n => <option key={n} value={n}>{n}×{n}</option>)}
            </select>
          </label>
          <button onClick={() => restart(game.size)}>Restart</button>
        </div>
      </div>

      <Board grid={game.board} />

      <div style={{display:'flex',gap:8,marginTop:12}}>
        <button onClick={() => move('UP')}>Up</button>
        <button onClick={() => move('LEFT')}>Left</button>
        <button onClick={() => move('RIGHT')}>Right</button>
        <button onClick={() => move('DOWN')}>Down</button>
      </div>

      {game.won && <div className="overlay">🎉 You reached 2048! 🎉</div>}
      {game.over && <div className="overlay">Game Over — no moves left</div>}
      {loading && <div style={{marginTop:8}}>Working...</div>}
    </div>
  )
}

function Board({ grid }){
  const size = grid.length
  const tileSize = Math.max(60, Math.floor(320 / size))
  return (
    <div className="board" style={{
      gridTemplateColumns: `repeat(${size}, ${tileSize}px)`,
      gridAutoRows: `${tileSize}px`
    }}>
      {grid.flat().map((v, i) => <Tile key={i} value={v} />)}
    </div>
  )
}

function Tile({ value }){
  const cls = value === 0 ? 'tile empty' : `tile tile-${value}`
  return <div className={cls}>{value !== 0 ? value : ''}</div>
}
