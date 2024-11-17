db.createUser({
  user: 'admin',
  pwd: 'aaaa999999',
  roles: [
    {
      role: 'readWrite',
      db: 'test'
    }
  ]
});
